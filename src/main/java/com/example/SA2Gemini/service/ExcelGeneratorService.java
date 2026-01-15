package com.example.SA2Gemini.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Generador simple de "Excel".
 *
 * Para no romper los endpoints existentes, devuelve un CSV (compatible con Excel) como bytes.
 * Si luego querés XLSX real, se puede reemplazar por Apache POI.
 */
@Service
public class ExcelGeneratorService {

    public byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> data, String title) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();

        if (title != null && !title.isBlank()) {
            sb.append(title).append("\n");
        }

        // headers
        if (headers != null && !headers.isEmpty()) {
            sb.append(String.join(",", headers)).append("\n");
        }

        // rows
        if (data != null) {
            for (List<Object> row : data) {
                if (row == null) continue;
                sb.append(String.join(",", row.stream()
                        .map(cell -> escapeCsv(cell == null ? "" : String.valueOf(cell)))
                        .toList()))
                  .append("\n");
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value;
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = v.replace("\"", "\"\"");
            v = "\"" + v + "\"";
        }
        return v;
    }
}
