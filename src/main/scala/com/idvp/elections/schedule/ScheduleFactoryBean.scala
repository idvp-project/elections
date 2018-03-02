package com.idvp.elections.schedule

import java.io.IOException
import java.util.{Objects, Properties}

import org.apache.commons.lang3.StringUtils
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.springframework.beans.factory.SmartFactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.{ConfigurableEnvironment, EnumerablePropertySource, Environment}
import org.springframework.stereotype.Component

import util.control.Breaks._
import scala.collection.JavaConverters._

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
//noinspection VarCouldBeVal
@Component
class ScheduleFactoryBean extends SmartFactoryBean[Scheduler] {

    @volatile private var scheduler: Scheduler = _

    @Autowired
    private var jobFactory: SpringAutowireJobFactory = _

    @Autowired
    private var environment: Environment = _

    override def getObject: Scheduler = {
        Option(scheduler) match {
            case Some(s) => s
            case None =>
                this.synchronized {
                    Option(scheduler) match {
                        case Some(s) => s
                        case None =>
                            val quartzProperties = getQuartzProperties(environment)
                            //misfire триггера уже при опоздании на 1 секунду
                            quartzProperties.setProperty("org.quartz.jobStore.misfireThreshold", "1000")
                            val factory = new StdSchedulerFactory(quartzProperties)
                            factory.initialize()
                            scheduler = factory.getScheduler
                            scheduler.setJobFactory(jobFactory)
                            scheduler.start()
                            scheduler
                    }
                }

        }
    }

    @throws[IOException]
    private def getQuartzProperties(env: Environment): Properties = {
        val properties = getDefaultProperties
        if (!env.isInstanceOf[ConfigurableEnvironment]) {
            return properties
        }

        val environment = env.asInstanceOf[ConfigurableEnvironment]
        for (propertySource <- environment.getPropertySources.asScala) {
            breakable {
                if (!propertySource.isInstanceOf[EnumerablePropertySource[_]]) {
                    break
                } else {
                    val enumerablePropertySource = propertySource.asInstanceOf[EnumerablePropertySource[_]]
                    for (code <- enumerablePropertySource.getPropertyNames) {
                        if (StringUtils.startsWith(code, "org.quartz")) {
                            val value = propertySource.getProperty(code)
                            properties.setProperty(code, Objects.toString(value))
                        }
                    }
                }
            }
        }

        properties
    }

    @throws[IOException]
    private def getDefaultProperties: Properties = {
        val result = new Properties

        var cl = getClass.getClassLoader

        if (cl == null) {
            cl = Thread.currentThread.getContextClassLoader
        }

        if (cl == null) {
            return result
        }

        var in = cl.getResourceAsStream("quartz.properties")

        if (in == null) {
            in = cl.getResourceAsStream("/quartz.properties")
        }

        if (in == null) {
            in = cl.getResourceAsStream("org/quartz/quartz.properties")
        }

        if (in == null) {
            return result
        }

        try {
            result.load(in)
        }
        finally {
            in.close()
        }
        result
    }

    override def getObjectType: Class[_] = classOf[Scheduler]

    override def isSingleton = true

    override def isEagerInit = true

    override def isPrototype = false
}
