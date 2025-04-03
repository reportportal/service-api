package com.epam.ta.reportportal.core.tms.mapper.parser;

import com.epam.ta.reportportal.core.tms.dto.TmsDatasetDataRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class XlsxTmsDatasetParser implements TmsDatasetParser {

  @Override
  public TmsDatasetFileType getTmsDatasetFileType() {
    return TmsDatasetFileType.XLSX;
  }

  @Override
  @SneakyThrows
  public List<TmsDatasetRQ> parse(MultipartFile file) {
    var datasetMap = new LinkedHashMap<String, List<TmsDatasetDataRQ>>();

    try (var fis = file.getInputStream(); var workbook = WorkbookFactory.create(fis)) {

      // working with first sheet
      var sheet = workbook.getSheetAt(0);
      var rowIterator = sheet.iterator();

      if (rowIterator.hasNext()) {
        rowIterator.next(); // skip header
      }

      while (rowIterator.hasNext()) {
        var row = rowIterator.next();

        var datasetName = getCellValue(row.getCell(0));
        var datasetDataKey = getCellValue(row.getCell(1));
        var datasetDataValue = getCellValue(row.getCell(2));

        datasetMap
            .computeIfAbsent(datasetName, emptyList -> new ArrayList<>())
            .add(new TmsDatasetDataRQ(datasetDataKey, datasetDataValue));
      }
    }
    return datasetMap
        .entrySet()
        .stream()
        .map(dataset -> TmsDatasetRQ.builder().name(dataset.getKey()).attributes(dataset.getValue()).build())
        .toList();
  }

  private String getCellValue(Cell cell) {
    if (Objects.isNull(cell)) {
      throw new IllegalArgumentException("Unexpected xlsx sheet cell value");
    }
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue().trim();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue().toString();
        } else {
          return Double.toString(cell.getNumericCellValue());
        }
      case BOOLEAN:
        return Boolean.toString(cell.getBooleanCellValue());
      case FORMULA:
        return cell.getCellFormula();
      case BLANK:
        return "";
      default:
        return cell.toString();
    }
  }
}
