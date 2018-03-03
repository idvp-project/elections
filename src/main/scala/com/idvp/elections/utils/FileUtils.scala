package com.idvp.elections.utils

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.Comparator
import java.util.stream.Collectors

import scala.collection.JavaConverters._

import org.slf4j.LoggerFactory

/**
  * @author Oleg Zinoviev
  * @since 03.03.18.
  */
object FileUtils {

    private val logger = LoggerFactory.getLogger("com.idvp.elections.utils.FileUtils")


    def move(from: Path, to: Path): Unit = {
        Assert.notNull(from, "from")
        Assert.notNull(to, "to")

        try {
            try {
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch {
                case _: AtomicMoveNotSupportedException =>
                    Files.move(from, to, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch {
            case e: Exception =>
                logger.error(s"Ошибка при переносе файла $from", e)
                throw e
        }
    }

    def getLatestFile(path: Path, matcher: PathMatcher = null): Option[Path] = {
        Assert.notNull(path, "path")

        val latestPath = Files.list(path)
            .filter(p => Files.isRegularFile(p) && Files.isReadable(p))
            .filter(p => matcher == null || matcher.matches(p.getFileName))
            .sorted(Comparator.comparingLong(
                (p: Path) => Files.readAttributes(p, classOf[BasicFileAttributes]).lastModifiedTime().toMillis)
                .reversed())
            .findFirst()
            .orElse(null)

        Option(latestPath)
    }

    def getLatestFiles(path: Path, matcher: PathMatcher = null): Stream[Path] = {
        Assert.notNull(path, "path")

        val latestPath = Files.list(path)
            .filter(p => Files.isRegularFile(p) && Files.isReadable(p))
            .filter(p => matcher == null || matcher.matches(p.getFileName))
            .sorted(Comparator.comparingLong(
                (p: Path) => Files.readAttributes(p, classOf[BasicFileAttributes]).lastModifiedTime().toMillis)
                .reversed())
            .collect(Collectors.toList())

        latestPath.asScala.toStream
    }


}
