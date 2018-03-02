package com.idvp.elections.utils

import scala.util.{Failure, Success, Try}

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
object Using {
    def using[A <: AutoCloseable, B](resource: A)(block: A => B): B = {

        Try(block(resource)) match {
            case Success(result) =>
                resource.close()
                result
            case Failure(e) =>
                resource.close()
                throw e
        }
    }
}
