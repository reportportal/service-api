package com.epam.ta.reportportal.core.tms.mapper.importer;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class TmsTestCaseJsonImporter implements TmsTestCaseImporter {

  private final ObjectMapper objectMapper;

  @Override
  public TmsTestCaseImportFormat getSupportedFormat() {
    return TmsTestCaseImportFormat.JSON;
  }

  @Override
  @SneakyThrows
  public List<TmsTestCaseRQ> importFromFile(MultipartFile file) {
    try (var inputStream = file.getInputStream()) {
      return objectMapper.readValue(inputStream, new TypeReference<>() {
      });
    }
  }
}
