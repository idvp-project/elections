package com.idvp.elections.schedule

import org.quartz.spi.{JobFactory, TriggerFiredBundle}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.scheduling.quartz.AdaptableJobFactory
import org.springframework.stereotype.Component

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
@Component
class SpringAutowireJobFactory extends AdaptableJobFactory with JobFactory {

    //noinspection VarCouldBeVal
    @Autowired
    private var beanFactory: AutowireCapableBeanFactory = _

    @throws[Exception]
    override protected def createJobInstance(bundle: TriggerFiredBundle): AnyRef = {
        val job = super.createJobInstance(bundle)
        if (job == null) {
            return null
        }
        beanFactory.autowireBean(job)
        job
    }
}
