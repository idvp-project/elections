package com.idvp.elections.transformation

import java.nio.file.Path

/**
  * @author Oleg Zinoviev
  * @since 01.03.18.
  */
trait Transformation {
    def output(): String
    def transform(source: Path): Option[Path]
}
