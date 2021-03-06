package com.idvp.elections.service

import java.io.InputStream


/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
trait ElectionsService {
    def getLatest: Option[InputStream]
    def forceUpdate(externalPath: String = null): Unit
}
