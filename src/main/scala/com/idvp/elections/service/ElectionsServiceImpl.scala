package com.idvp.elections.service

import java.io.InputStream
import java.nio.file.{FileSystems, Files, Path, StandardOpenOption}
import java.util.Collections
import javax.annotation.PostConstruct

import com.idvp.elections.transformation.Transformation
import com.idvp.elections.utils.FileUtils
import com.idvp.elections.utils.TargetPath.TargetPath
import org.apache.commons.lang3.StringUtils
import org.quartz._
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Service

import scala.util.{Failure, Success, Try}

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
//noinspection VarCouldBeVal
@Service
class ElectionsServiceImpl extends ElectionsService {

    private val logger = LoggerFactory.getLogger(classOf[ElectionsService])

    @Autowired
    private var scheduler: Scheduler = _

    @Autowired
    private var transformation: Transformation = _

    @Value("${com.idvp.elections.target.path}")
    private var targetPath: String = _

    @Value("${com.idvp.elections.job.cron:}")
    private var cron: String = _

    private var path: Path = _

    @PostConstruct
    def init(): Unit = {
        path = targetPath.createTargetPath

        if (StringUtils.isNotEmpty(cron)) {
            val trigger = TriggerBuilder.newTrigger()
                .withIdentity(ElectionsJob.JOB_NAME)
                .withSchedule(CronScheduleBuilder
                    .cronSchedule(cron)
                    .withMisfireHandlingInstructionDoNothing)
                .startNow()
                .build()

            val job = createJob

            scheduler.scheduleJob(job, Collections.singleton(trigger), true)
        }
    }

    override def getLatest: Option[InputStream] = {
        val matcher = FileSystems.getDefault.getPathMatcher(s"glob:*.${transformation.output()}")

        val latestPath = FileUtils.getLatestFile(path, matcher)

        latestPath match {
            case None => None
            case Some(p) =>
                Try(Files.newInputStream(p, StandardOpenOption.READ)) match {
                    case Success(stream) => Some(stream)
                    case Failure(ex) =>
                        logger.error(s"Ошибка при чтении файла $p", ex)
                        None
                }
        }
    }

    override def forceUpdate(externalPath: String): Unit = {
        val key = new JobKey(ElectionsJob.JOB_NAME)
        if (scheduler.checkExists(key)) {

            if (externalPath == null) {
                scheduler.triggerJob(key)
            } else {
                val map = new JobDataMap()
                map.put(ElectionsJob.EXTERNAL_FILE_PATH, externalPath)
                scheduler.triggerJob(key, map)
            }
        } else {
            scheduler.scheduleJob(createJob, TriggerBuilder.newTrigger()
                .startNow()
                .usingJobData(ElectionsJob.EXTERNAL_FILE_PATH, externalPath)
                .build())
        }
    }

    private def createJob = {
        JobBuilder.newJob(classOf[ElectionsJob])
            .withIdentity(ElectionsJob.JOB_NAME)
            .build()
    }
}
