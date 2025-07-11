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

package com.epam.ta.reportportal.util;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Konstantin Antipin
 */
public class ControllerUtils {

  /**
   * Tries to find request part or file with specified name in multipart attachments map.
   *
   * @param filename File name
   * @param files    Files map
   * @return Found file
   */
  public static MultipartFile findByFileName(String filename,
      MultiValuedMap<String, MultipartFile> files) {
    /* Request part name? */
    if (files.containsKey(filename)) {
      var multipartFile = files.get(filename).stream()
          .findFirst()
          .get();
      files.get(filename).remove(multipartFile);
      return multipartFile;
    }
    /* Filename? */
    for (MultipartFile file : files.values()) {
      if (filename.equals(file.getOriginalFilename())) {
        return file;
      }
    }
    return null;
  }

  public static Long safeParseLong(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "The provided parameter must be a number");
    }
  }

  public static Integer safeParseInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "The provided parameter must be a number");
    }
  }

  public static void validateSaveRQ(Validator validator, SaveLogRQ saveLogRQ) {
    Set<ConstraintViolation<SaveLogRQ>> constraintViolations = validator.validate(saveLogRQ);
    if (constraintViolations != null && !constraintViolations.isEmpty()) {
      StringBuilder messageBuilder = new StringBuilder();
      for (ConstraintViolation<SaveLogRQ> constraintViolation : constraintViolations) {
        messageBuilder.append("[");
        messageBuilder.append("Incorrect value in save log request '");
        messageBuilder.append(constraintViolation.getInvalidValue());
        messageBuilder.append("' in field '");
        Iterator<Path.Node> iterator = constraintViolation.getPropertyPath().iterator();
        messageBuilder.append(iterator.hasNext() ? iterator.next().getName() : "");
        messageBuilder.append("'.]");
      }
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, messageBuilder.toString());
    }
  }

  public static MultiValuedMap<String, MultipartFile> getUploadedFiles(HttpServletRequest request) {
    MultiValuedMap<String, MultipartFile> uploadedFiles = new ArrayListValuedHashMap<>();
    if (request instanceof MultipartHttpServletRequest multipartRequest) {
      MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
      for (List<MultipartFile> multipartFiles : multiFileMap.values()) {
        for (MultipartFile file : multipartFiles) {
          uploadedFiles.put(file.getOriginalFilename(), file);
        }
      }
    }
    return uploadedFiles;
  }

  public static Direction parseSortDirection(String order) {
    if (order == null) {
      return Direction.ASC;
    }
    return order.equalsIgnoreCase(Direction.DESC.name()) ? Direction.DESC : Direction.ASC;
  }

  public static Pageable getPageable(String sortBy, String order, int offset, int limit) {
    var sortDirection = parseSortDirection(order);
    //TODO: switch to ScrollPosition after migration to Spring Data 3.1
    return OffsetRequest.of(offset, limit, Sort.by(sortDirection, sortBy));
  }
}
