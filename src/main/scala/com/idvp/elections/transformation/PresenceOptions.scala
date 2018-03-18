package com.idvp.elections.transformation

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
//noinspection VarCouldBeVal
@Component
class PresenceOptions {

    @Value("${com.idvp.presence.transformation.sheet:0}")
    private var sheet: Int = _

    @Value("${com.idvp.presence.transformation.row.start:11}")
    private var rowStart: Int = _

    @Value("${com.idvp.presence.transformation.row.end:99}")
    private var rowEnd: Int = _

    @Value("${com.idvp.presence.transformation.col.start:1}")
    private var colStart: Int = _

    @Value("${com.idvp.presence.transformation.col.end:5}")
    private var colEnd: Int = _

    @Value("${com.idvp.presence.transformation.headers:true}")
    private var containsHeaders: Boolean = _

    def getSheet: Int = sheet

    def getRange: (Int, Int, Int, Int) = (rowStart, colStart, rowEnd, colEnd)

    def isContainsHeaders: Boolean = containsHeaders
}
