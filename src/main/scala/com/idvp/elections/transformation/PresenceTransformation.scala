package com.idvp.elections.transformation

import java.nio.file.Path

/**
  * @author Oleg Zinoviev
  * @since 18.03.18.
  */
trait PresenceTransformation {
    def output(): String
    def transform(source: Path): Option[Path]
}
