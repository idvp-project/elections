package com.idvp.elections.transformation

import java.nio.file._
import java.util

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.idvp.elections.api.model.Item
import com.idvp.elections.utils.Assert
import com.idvp.elections.utils.Using.using
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel._
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * @author Oleg Zinoviev
  * @since 01.03.18.
  */
//noinspection VarCouldBeVal
@Component
class TransformationImpl extends Transformation {

    private val mapper = new ObjectMapper()
        .configure(SerializationFeature.CLOSE_CLOSEABLE, true)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)

    private val logger = LoggerFactory.getLogger(classOf[Transformation])

    @Autowired
    private var options: Options = _

    override def output(): String = "json"

    override def transform(source: Path): Option[Path] = {
        Assert.notNull(source, "source")

        if (!Files.exists(source) || !Files.isReadable(source)) {
            logger.error(s"Файл $source отсутствует или не может быть прочитан")
            return None
        }


        val data = Try(readWorkbook(source)) match {
            case Success(rows) =>
                Option(rows)
            case Failure(e) =>
                logger.error(s"Ошибка при чтении данных из файла $source", e)
                None
        }

        if (data.isEmpty) {
            return None
        }

        val tempPath = Files.createTempFile("elections", ".transformed.tmp")

        val result = Try(using(Files.newOutputStream(tempPath,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
            stream => mapper.writeValue(stream, data.get)
        })

        if (result.isFailure) {
            logger.error(s"Ошибка при сохранении результата трансформации в файл $tempPath", result.failed.get)
            None
        }

        Some(tempPath)
    }

    private def readWorkbook(file: Path): util.List[Item] =
        using(WorkbookFactory.create(file.toFile)) {
            workbook => {
                val formatter = new DataFormatter()
                val evaluator = workbook.getCreationHelper.createFormulaEvaluator()

                val sheet = workbook.getSheetAt(options.getSheet)
                Assert.notNull(sheet)

                var (startRow, startCol, _, _) = options.getRange
                if (options.isContainsHeaders) {
                    startRow += 1
                    startCol += 1
                }

                val (rowHeaders, colHeaders) = extractHeaders(sheet, formatter, evaluator)

                val cellValue = readCellValue(_: Cell, formatter, evaluator)

                rowHeaders
                    .toStream
                    .zipWithIndex
                    .flatMap(row => colHeaders.toStream.zipWithIndex.map(col => (row, col)))
                    .map(index => {
                        val ((rowHeader, rowIndex), (colHeader, colIndex)) = index

                        val cell = Option(sheet.getRow(rowIndex + startRow))
                            .map(row => row.getCell(colIndex + startCol))
                            .orNull

                        Item(rowHeader, colHeader, cellValue(cell))
                    })
                    .toList
                    .asJava
            }

        }

    private def extractHeaders(sheet: Sheet,
                               formatter: DataFormatter, evaluator:
                               FormulaEvaluator): (Array[String], Array[String]) = {
        Assert.notNull(formatter, "formatter")

        val (startRow, startCol, endRow, endCol) = options.getRange
        val cellValue = readCellValue(_: Cell, formatter, evaluator)

        //Индексы 0-based
        val rowHeaders: Array[String] = new Array[String](endRow - startRow)
        val colHeaders: Array[String] = new Array[String](endCol - startCol)

        if (options.isContainsHeaders) {

            //Пропускаем пересечение row и col headers
            val rowSeq = (startRow + 1).to(endRow)
            val colSeq = (startCol + 1).to(endCol)

            val regions = sheet.getMergedRegions.asScala

            sheet.rowIterator()
                .asScala
                .filter(row => rowSeq.contains(row.getRowNum))
                .map(row => (row, row.getRowNum))
                .foreach { case (row, index) =>

                    val cell = regions
                        .toStream
                        .find(r => r.containsRow(index) && r.containsColumn(startCol))
                        .flatMap(r => Option(sheet.getRow(r.getFirstRow)).map(sr => sr.getCell(r.getFirstColumn)))
                        .getOrElse(row.getCell(startCol))

                    val value = cellValue(cell)

                    if (options.getPreviousHeaderIfMissing && StringUtils.isBlank(value)) {
                        rowHeaders.view(0, index - startRow - 1)
                            .reverse
                            .find(h => !StringUtils.isBlank(h)) match {
                            case None =>
                                rowHeaders(index - startRow - 1) = value
                            case Some(h) =>
                                rowHeaders(index - startRow - 1) = h + options.getMissingHeaderSuffix
                        }

                    } else {
                        rowHeaders(index - startRow - 1) = value
                    }
                }

            Option(sheet.getRow(startRow)) match {
                case Some(row) =>
                    row.cellIterator()
                        .asScala
                        .filter(cell => colSeq.contains(cell.getColumnIndex))
                        .map(cell => (cell, cell.getColumnIndex))
                        .foreach { case (cell, index) =>

                            val result = regions
                                .toStream
                                .find(r => r.containsRow(startRow) && r.containsColumn(index))
                                .flatMap(r => Option(sheet.getRow(r.getFirstRow)).map(sr => sr.getCell(r.getFirstColumn)))
                                .getOrElse(cell)

                            val value = cellValue(result)

                            if (options.getPreviousHeaderIfMissing && StringUtils.isBlank(value)) {
                                colHeaders.view(0, index - startCol - 1)
                                    .reverse
                                    .find(h => !StringUtils.isBlank(h)) match {
                                    case None =>
                                        colHeaders(index - startCol - 1) = value
                                    case Some(h) =>
                                        colHeaders(index - startCol - 1) = h + options.getMissingHeaderSuffix
                                }
                            } else {
                                colHeaders(index - startCol - 1) = value
                            }
                        }

                case None =>
                    colHeaders.indices
                        .foreach(index => rowHeaders(index) = s"Col $index")
            }

        } else {
            rowHeaders.indices
                .foreach(index => rowHeaders(index) = s"Row $index")
            colHeaders.indices
                .foreach(index => rowHeaders(index) = s"Col $index")
        }

        (rowHeaders, colHeaders)
    }

    private def readCellValue(cell: Cell,
                              formatter: DataFormatter,
                              evaluator: FormulaEvaluator): String = {
        Assert.notNull(formatter, "formatter")

        Option(cell)
            .map(cell => formatter.formatCellValue(cell, evaluator))
            .orNull
    }


}