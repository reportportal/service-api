/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.launch.export;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.DataStoreService;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.epam.reportportal.rules.exception.ErrorType;

/**
 * Service responsible for writing files into a ZIP output stream using file IDs.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class AttachmentZipService {

  private final DataStoreService dataStoreService;

  public AttachmentZipService(@Qualifier("attachmentDataStoreService") DataStoreService dataStoreService) {
    this.dataStoreService = dataStoreService;
  }

  /**
   * Streams a file into the provided ZIP output stream.
   *
   * @param fileId   the ID of the file to load from the data store
   * @param filePath the path where the file will be placed in the ZIP
   * @param zipOut   the output ZIP stream
   */
  public void writeToZip(String fileId, String filePath, ZipOutputStream zipOut) {
    try (InputStream input = dataStoreService.load(fileId).orElse(null)) {
      if (input == null) {
        return;
      }
      zipOut.putNextEntry(new ZipEntry(filePath));
      input.transferTo(zipOut);
      zipOut.closeEntry();
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Cannot write file to ZIP: " + filePath, e);
    }
  }

}
