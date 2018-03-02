package com.idvp.elections.utils

import java.nio.file.{Files, Path, Paths}

import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
object TargetPath {
    implicit class TargetPath(path: String) {
        private val logger = LoggerFactory.getLogger(classOf[TargetPath])
        def createTargetPath: Path = {
            Try({
                val p = Paths.get(path)
                if (!Files.exists(p)) {
                    Files.createDirectories(p)
                }
                p
            }) match {
                case Success(p) => p
                case Failure(ex) =>
                    logger.error(s"Не удалось создать директорию $path", ex)
                    throw ex
            }
        }
    }

}
