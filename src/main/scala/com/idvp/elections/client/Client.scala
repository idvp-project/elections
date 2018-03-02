package com.idvp.elections.client

import java.nio.file.Path

/**
  * @author Oleg Zinoviev
  * @since 01.03.18.
  */
trait Client {
    def download(): Option[Path]
}
