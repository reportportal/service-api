package com.epam.reportportal.base.core.tms.mapper.parser;

import com.epam.reportportal.base.core.tms.dto.TmsDatasetRQ;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TmsDatasetParser {

  TmsDatasetFileType getTmsDatasetFileType();

  List<TmsDatasetRQ> parse(MultipartFile file);
}
