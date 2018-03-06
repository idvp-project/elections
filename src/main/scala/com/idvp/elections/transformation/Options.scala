package com.idvp.elections.transformation

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
//noinspection VarCouldBeVal
@Component
class Options {

    @Value("${com.idvp.election.transformation.sheet:0}")
    private var sheet: Int = _

    @Value("${com.idvp.election.transformation.row.start:10}")
    private var rowStart: Int = _

    @Value("${com.idvp.election.transformation.row.end:39}")
    private var rowEnd: Int = _

    @Value("${com.idvp.election.transformation.col.start:1}")
    private var colStart: Int = _

    @Value("${com.idvp.election.transformation.col.end:87}")
    private var colEnd: Int = _

    @Value("${com.idvp.election.transformation.headers:true}")
    private var containsHeaders: Boolean = _

    @Value("${com.idvp.election.transformation.headers.missing.previous:true}")
    private var previousHeaderIfMissing: Boolean = _

    @Value("${com.idvp.election.transformation.headers.missing.suffix: %}")
    private var missingHeaderSuffix: String = _

    def getSheet: Int = sheet

    def getRange: (Int, Int, Int, Int) = (rowStart, colStart, rowEnd, colEnd)

    def isContainsHeaders: Boolean = containsHeaders

    def getPreviousHeaderIfMissing: Boolean = previousHeaderIfMissing

    def getMissingHeaderSuffix: String = missingHeaderSuffix
}
