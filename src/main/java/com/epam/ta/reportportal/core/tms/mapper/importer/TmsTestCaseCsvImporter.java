package com.epam.ta.reportportal.core.tms.mapper.importer;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class TmsTestCaseCsvImporter implements TmsTestCaseImporter {

  private static final String[] EXPECTED_HEADERS = {"name", "description", "testFolder",
      "priority"};

  @Override
  public TmsTestCaseImportFormat getSupportedFormat() {
    return TmsTestCaseImportFormat.CSV;
  }

  @Override
  @SneakyThrows
  public List<TmsTestCaseRQ> importFromFile(MultipartFile file) {
    List<TmsTestCaseRQ> testCases = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        var csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
            .setHeader(EXPECTED_HEADERS)
            .setSkipHeaderRecord(true)
            .build())) {

      for (var csvRecord : csvParser) {
        if (csvRecord.size() < EXPECTED_HEADERS.length) {
          throw new IllegalArgumentException(
              "Invalid CSV format. Expected columns: " + String.join(", ", EXPECTED_HEADERS));
        }

        TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
        testCaseRQ.setName(csvRecord.get("name"));
        testCaseRQ.setDescription(csvRecord.get("description"));
        testCaseRQ.setPriority(csvRecord.get("priority"));

        if (StringUtils.hasText(csvRecord.get("testFolder"))) {
          TmsTestCaseTestFolderRQ testFolderRQ = new TmsTestCaseTestFolderRQ();
          testFolderRQ.setName(csvRecord.get("testFolder"));
          testCaseRQ.setTestFolder(testFolderRQ);
        }

        testCases.add(testCaseRQ);
      }
    }
    return testCases;
  }
}
