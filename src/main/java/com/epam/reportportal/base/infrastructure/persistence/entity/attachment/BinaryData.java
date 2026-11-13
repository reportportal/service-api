/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.attachment;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * Binary data representation. Contains only input stream and data type. Introduced to simplify store/retrieve
 * operations
 *
 * @author Andrei Varabyeu
 */
public class BinaryData {

  private String fileName;

  /**
   * MIME Type of Binary Data
   */
  private final String contentType;

  /**
   * Data Stream
   */
  private final InputStream inputStream;

  /**
   * Content length
   */
  private final Long length;

  public BinaryData(String contentType, Long length, InputStream inputStream) {
    this.contentType = contentType;
    this.inputStream = inputStream;
    this.length = length;
  }

  public BinaryData(String fileName, String contentType, Long length, InputStream inputStream) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.length = length;
    this.inputStream = inputStream;
  }

  public BinaryData(MultipartFile multipartFile) {
    this.contentType = multipartFile.getContentType();
    this.length = multipartFile.getSize();

    try {
      this.inputStream = multipartFile.getInputStream();
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to create binary data from multipart file");
    }
  }

  public String getFileName() {
    return fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public Long getLength() {
    return length;
  }

}
