package kz.zsanzharko.service.reporter.generator;

import kz.zsanzharko.model.QuestionModel;
import lombok.extern.slf4j.Slf4j;
import kz.zsanzharko.model.Report;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.SortedSet;
import java.util.TreeSet;

@Slf4j
public class ExcelReportService implements ReportExporter {

    @Override
    public InputFile export(Report report) {
        try (Workbook workbook = new XSSFWorkbook()) {
            int rowIndex = 0;
            int columnLength = 0;
            boolean initialsHeaderIsSet = false;
            var initials = new TreeSet<>(report.getInitials());
            var dailySheet = workbook.createSheet("Ежедневная");
            var reporterSheet = workbook.createSheet("Отчет");
            for (var reportEntry : report.getReport().entrySet()) {
                int cellIndex = 0;
                Row row = dailySheet.createRow(rowIndex++);
                Cell cell = row.createCell(cellIndex++);
                //set date
                cell.setCellValue(reportEntry.getKey());
                cell.setCellStyle(getDateStyle(workbook));
                // set initials
                //set column size for autosize columns
                if (columnLength < initials.size()) {
                    columnLength = initials.size();
                }
                if (!initialsHeaderIsSet) {
                    setInitialsHeader(initials, workbook, row, cellIndex);
                    initialsHeaderIsSet = true;
                }
                var dataStyle = workbook.createCellStyle();
                addAllBorders(dataStyle);
                // starting set question by user
                for (QuestionModel question : report.getQuestionModelList()) {
                    // new row for each question
                    row = dailySheet.createRow(rowIndex++);
                    // new cell for each initial
                    cellIndex = 0;
                    // setting first cell question title
                    cell = row.createCell(cellIndex++);
                    cell.setCellValue(question.getTitle());
                    cell.setCellStyle(dataStyle);

                    for (String initial : initials) {
                        if (reportEntry.getValue().containsKey(initial)) {
                            for (var qa : reportEntry.getValue().get(initial)) {
                                if (qa.getQuestionId().equals(question.getId())) {
                                    cell = row.createCell(cellIndex);
                                    cell.setCellValue(qa.getAnswer());
                                    cell.setCellStyle(dataStyle);
                                    break;
                                }
                            }
                        } else {
                            cell = row.createCell(cellIndex);
                            cell.setCellValue("");
                            cell.setCellStyle(dataStyle);
                        }
                        cellIndex++;
                    }
                }
            }
            // set formula for daily report
            {
                var style = workbook.createCellStyle();
                addAllBorders(style);
                addYellowColor(style);
                for (int rowNum = 0,
                     skipCounter = report.getQuestionModelList().size() + 1;
                     rowNum < rowIndex; rowNum++) {
                    if (rowNum == 0) continue;
                    if (rowNum % skipCounter == 0) continue;
                    var row = dailySheet.getRow(rowNum);
                    var lastCell = row.getLastCellNum() - 1;
                    var totalCell = row.createCell(lastCell + 1);
                    var formula = String.format("SUM(B%d:%s%d)",
                            rowNum + 1, CellReference.convertNumToColString(lastCell), rowNum + 1
                    );
                    log.debug("Setting formula: {}", formula);
                    totalCell.setCellFormula(formula);
                    totalCell.setCellStyle(style);
                }
            }
            for (int i = 0; i < columnLength + 2; i++) {
                dailySheet.autoSizeColumn(i);
            }
            return getStream(workbook);
        } catch (IOException e) {
            throw new RuntimeException("Error exporting report to Excel: " + e.getMessage(), e);
        }
    }

    private void setInitialsHeader(SortedSet<String> initials, Workbook workbook, Row row, int cellIndex) {
        //style
        CellStyle style = workbook.createCellStyle();
        style.setFont(getBoldFont(workbook));
        addAllBorders(style);
        for (var initial : initials) {
            var cell = row.createCell(cellIndex++);
            cell.setCellValue(initial);
            cell.setCellStyle(style);
        }
        // add total
        var cell = row.createCell(cellIndex);
        cell.setCellValue("Сводный итог");
        cell.setCellStyle(style);
    }

    private InputFile getStream(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        String fileName = String.format("report_%s.xlsx",
                new Date(System.currentTimeMillis()));
        return new InputFile().setMedia(
                new ByteArrayInputStream(outputStream.toByteArray()),
                fileName);
    }

    private CellStyle getDateStyle(Workbook workbook) {
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setFont(getBoldFont(workbook));
        dateStyle.setDataFormat((short) 14);
        dateStyle.setFillBackgroundColor(IndexedColors.YELLOW.index);
        addAllBorders(dateStyle);
        return dateStyle;
    }

    private Font getBoldFont(Workbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        return boldFont;
    }

    private void addAllBorders(CellStyle style) {
        // top
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        // bottom
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        // right
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        // left
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
    }

    private void addYellowColor(CellStyle style) {
        style.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
    }
}
