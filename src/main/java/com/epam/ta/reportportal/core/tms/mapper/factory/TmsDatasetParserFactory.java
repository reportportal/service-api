package com.epam.ta.reportportal.core.tms.mapper.factory;

import com.epam.ta.reportportal.core.tms.mapper.parser.TmsDatasetFileType;
import com.epam.ta.reportportal.core.tms.mapper.parser.TmsDatasetParser;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class TmsDatasetParserFactory {

  private Map<TmsDatasetFileType, TmsDatasetParser> tmsDatasetParsers;

  @Autowired
  public void setTmsDatasetParsers(
      List<TmsDatasetParser> tmsDatasetParsers) {
    this.tmsDatasetParsers = tmsDatasetParsers
        .stream()
        .collect(
            Collectors.toMap(TmsDatasetParser::getTmsDatasetFileType, Function.identity())
        );
  }

  public TmsDatasetParser getParser(MultipartFile file) {
    var parser = tmsDatasetParsers.get(getFileType(file));
    if (Objects.isNull(parser)) {
      throw new UnsupportedOperationException("Unsupported file type.");
    } else {
      return parser;
    }
  }

  private TmsDatasetFileType getFileType(MultipartFile file) {
    if (Objects.isNull(file) || StringUtils.isBlank(file.getOriginalFilename())) {
      throw new IllegalArgumentException();
    }
    return TmsDatasetFileType.fromString(
        file.getOriginalFilename()
            .substring(file.getOriginalFilename().lastIndexOf(".") + 1)
    );
  }
}
