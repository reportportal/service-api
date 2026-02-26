package com.epam.reportportal.base.core.tms.mapper.importer;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRQ;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TmsTestCaseImporter {

  TmsTestCaseImportFormat getSupportedFormat();

  List<TmsTestCaseRQ> importFromFile(MultipartFile file);
}
