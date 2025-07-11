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

package com.epam.ta.reportportal.reporting.async.controller;


import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.ta.reportportal.util.ControllerUtils.findByFileName;
import static com.epam.ta.reportportal.util.ControllerUtils.getUploadedFiles;
import static com.epam.ta.reportportal.util.ControllerUtils.validateSaveRQ;
import static org.springframework.http.HttpStatus.CREATED;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.core.logging.HttpLogging;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.BatchElementCreatedRS;
import com.epam.ta.reportportal.ws.reporting.BatchSaveOperatingRS;
import com.epam.ta.reportportal.ws.reporting.Constants;
import com.epam.ta.reportportal.ws.reporting.EntryCreatedAsyncRS;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v2/{projectKey}/log")
@PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
@Tag(name = "Log Async", description = "Logs Async API collection")
public class LogAsyncController {

  private final ProjectExtractor projectExtractor;
  private final CreateLogHandler createLogHandler;
  private final Validator validator;

  @Autowired
  public LogAsyncController(ProjectExtractor projectExtractor,
      @Qualifier("logProducer") CreateLogHandler createLogHandler, Validator validator) {
    this.projectExtractor = projectExtractor;
    this.createLogHandler = createLogHandler;
    this.validator = validator;
  }

  /**
   * @deprecated in favour of
   * {@link LogAsyncController#createLogEntry(String, SaveLogRQ, ReportPortalUser)}
   * because of mapping collisions
   */
  /* Report client API */
  @Deprecated
  @HttpLogging
  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(CREATED)
  @Hidden
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedAsyncRS createLog(@PathVariable String projectKey,
      @RequestBody SaveLogRQ createLogRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    validateSaveRQ(validator, createLogRQ);
    return createLogHandler.createLog(createLogRQ, null,
        projectExtractor.extractMembershipDetails(user, projectKey));
  }

  @HttpLogging
  @PostMapping(value = "/entry", consumes = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(CREATED)
  @Operation(description = "Create log")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedAsyncRS createLogEntry(@PathVariable String projectKey,
      @RequestBody SaveLogRQ createLogRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    validateSaveRQ(validator, createLogRQ);
    return createLogHandler.createLog(createLogRQ, null,
        projectExtractor.extractMembershipDetails(user, projectKey));
  }

  @HttpLogging
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  @Operation(description = "Create log (batching operation)")
  // Specific handler should be added for springfox in case of similar POST
  // request mappings
  //	@Async
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public ResponseEntity<BatchSaveOperatingRS> createLog(@PathVariable String projectKey,
      @RequestPart(value = Constants.LOG_REQUEST_JSON_PART) SaveLogRQ[] createLogRQs,
      HttpServletRequest request,
      @AuthenticationPrincipal ReportPortalUser user) {

    /*
     * Since this is multipart request we can retrieve list of uploaded
     * attachments
     */
    MultiValuedMap<String, MultipartFile> uploadedFiles = getUploadedFiles(request);
    BatchSaveOperatingRS response = new BatchSaveOperatingRS();
    EntryCreatedAsyncRS responseItem;
    /* Go through all provided save log request items */
    for (SaveLogRQ createLogRq : createLogRQs) {
      try {
        validateSaveRQ(validator, createLogRq);
        String filename = createLogRq.getFile() == null ? null : createLogRq.getFile().getName();
        if (StringUtils.isEmpty(filename)) {
          /*
           * There is no filename in request. Use simple save
           * method
           */
          responseItem = createLog(projectKey, createLogRq, user);

        } else {
          /* Find by request part */
          MultipartFile data = findByFileName(filename, uploadedFiles);
          BusinessRule.expect(data, Predicates.notNull()).verify(
              ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
              Suppliers.formattedSupplier("There is no request part or file with name {}", filename)
          );
          /*
           * If provided content type is null or this is octet
           * stream, try to detect real content type of binary
           * data
           */
          responseItem = createLogHandler.createLog(createLogRq, data,
              projectExtractor.extractMembershipDetails(user, projectKey));
        }
        response.addResponse(new BatchElementCreatedRS(responseItem.getId()));
      } catch (Exception e) {
        response.addResponse(new BatchElementCreatedRS(ExceptionUtils.getStackTrace(e),
            ExceptionUtils.getMessage(e)));
      }
    }
    return new ResponseEntity<>(response, CREATED);
  }
}
