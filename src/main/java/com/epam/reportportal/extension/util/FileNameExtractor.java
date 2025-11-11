package com.epam.reportportal.extension.util;

import com.epam.reportportal.infrastructure.persistence.filesystem.DataEncoder;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

/**
 * Decodes file path encoded by {@link DataEncoder} and extracts file name from it
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class FileNameExtractor {

  @Nullable
  public static String extractFileName(DataEncoder dataEncoder, @Nullable String fileId) {

    if (StringUtils.isBlank(fileId)) {
      return fileId;
    }

    return String.valueOf(Paths.get(dataEncoder.decode(fileId)).getFileName());
  }
}
