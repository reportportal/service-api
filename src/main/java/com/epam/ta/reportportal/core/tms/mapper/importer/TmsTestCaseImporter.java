package com.epam.ta.reportportal.core.tms.mapper.importer;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TmsTestCaseImporter {

  TmsTestCaseImportFormat getSupportedFormat();

  List<TmsTestCaseRQ> importFromFile(MultipartFile file);
}
