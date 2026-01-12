package com.example.SA2Gemini.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExcelGeneratorService {

    public byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> data, String title) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Estilos
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            int currentRow = 0;
            
            // Título
            if (title != null && !title.isEmpty()) {
                Row titleRow = sheet.createRow(currentRow++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(title);
                titleCell.setCellStyle(titleStyle);
                
                // Merge cells para el título
                if (headers.size() > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));
                }
                currentRow++; // Línea en blanco
            }
            
            // Headers
            Row headerRow = sheet.createRow(currentRow++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Data
            for (List<Object> rowData : data) {
                Row row = sheet.createRow(currentRow++);
                for (int i = 0; i < rowData.size() && i < headers.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = rowData.get(i);
                    
                    if (value == null) {
                        cell.setCellValue("");
                        cell.setCellStyle(dataStyle);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                        cell.setCellStyle(numberStyle);
                    } else if (value instanceof LocalDate) {
                        cell.setCellValue(((LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        cell.setCellStyle(dateStyle);
                    } else {
                        cell.setCellValue(value.toString());
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convertir a bytes
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
        
        return style;
    }
}