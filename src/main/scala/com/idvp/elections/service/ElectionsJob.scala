package com.idvp.elections.service

import java.nio.file._

import com.idvp.elections.client.Client
import com.idvp.elections.transformation.Transformation
import com.idvp.elections.utils.TargetPath.TargetPath
import com.idvp.elections.utils.{Assert, FileUtils}
import org.quartz.{DisallowConcurrentExecution, Job, JobExecutionContext}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}

import scala.util.{Failure, Success, Try}

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
//noinspection VarCouldBeVal
@DisallowConcurrentExecution
class ElectionsJob extends Job {

    private val logger = LoggerFactory.getLogger(classOf[ElectionsJob])

    @Value("${com.idvp.elections.target.path}")
    private var targetPath: String = _

    @Value("${com.idvp.elections.backup.path:old}")
    private var backupPath: String = _

    @Value("${com.idvp.elections.backup.keep:5}")
    private var backupKeep: Int = _

    @Autowired
    private var client: Client = _

    @Autowired
    private var transformation: Transformation = _

    override def execute(context: JobExecutionContext): Unit = {
        Assert.notNull(context, "context")

        val externalPath = Option(context.getMergedJobDataMap.getString(ElectionsJob.EXTERNAL_FILE_PATH))

        val path = externalPath match {
            case Some(p) =>
                try {
                    val result = Paths.get(p)
                    if (Files.notExists(result) || !Files.isReadable(result)) {
                        throw new InvalidPathException(p, "path")
                    }

                    Option(result)
                } catch {
                    case e: InvalidPathException =>
                        logger.error(s"Передан некорректный путь к файлу $p", e)
                        None
                }

            case None =>
                client.download()
        }

        if (path.isEmpty) {
            logger.error("Ошибка при загрузке файла")
            return
        }

        try {
            val transformed = transformation.transform(path.get)
            if (transformed.isEmpty) {
                logger.error(s"Ошибка при трансформации файла ${path.get}")
                return
            }

            val target = targetPath.createTargetPath
            val copyTo = target.resolve(s"${System.currentTimeMillis()}.${transformation.output()}")

            FileUtils.move(transformed.get, copyTo)


            try {
                val backup = target.resolve(backupPath).toAbsolutePath.toString.createTargetPath

                val (_, files) = FileUtils.getLatestFiles(target)
                    .splitAt(backupKeep)

                files.foreach(p => FileUtils.move(p, backup.resolve(p.getFileName)))

            } catch {
                case e: Exception =>
                    logger.error(s"Ошибка при переносе файлов в backup-директорию", e)
            }

        } finally {
            Try (Files.delete(path.get)) match {
                case Failure(ex) =>
                    logger.error(s"Ошибка при удалении файла ${path.get}", ex)
                case Success(_) =>
            }
        }

    }
}

object ElectionsJob {
    val EXTERNAL_FILE_PATH = "EXTERNAL_FILE_PATH"
    val JOB_NAME = "ElectionsJob"
}
