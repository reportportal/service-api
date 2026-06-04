package com.epam.reportportal.base.core.tms.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class TmsTestCasePreparationForImportResult {

  private List<PreparedTestCase> preparedTestCases = new ArrayList<>();

  private List<List<String>> uniquePaths = new ArrayList<>();

  private Set<String> uniqueAttributeKeys = new HashSet<>();

  private List<String> validationErrors = new ArrayList<>();

  public boolean hasErrors() {
    return !validationErrors.isEmpty();
  }

  public void addPreparedTestCase(PreparedTestCase prepared) {
    preparedTestCases.add(prepared);
  }

  public void addUniquePath(List<String> path) {
    if (path != null && !path.isEmpty() && !uniquePaths.contains(path)) {
      uniquePaths.add(path);
    }
  }

  public void addAttributeKeys(List<TmsTestCaseAttributeImportRQ> attributes) {
    if (attributes != null) {
      attributes.stream()
          .map(TmsTestCaseAttributeImportRQ::getKey)
          .filter(key -> key != null && !key.isBlank())
          .forEach(uniqueAttributeKeys::add);
    }
  }

  public void addError(String error) {
    validationErrors.add(error);
  }
}
