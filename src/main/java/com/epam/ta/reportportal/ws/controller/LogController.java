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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.querygen.Condition.UNDR;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.util.ControllerUtils.findByFileName;
import static com.epam.ta.reportportal.util.ControllerUtils.getUploadedFiles;
import static com.epam.ta.reportportal.util.ControllerUtils.validateSaveRQ;
import static com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver.DEFAULT_FILTER_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.auto.SearchLogService;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.core.log.DeleteLogHandler;
import com.epam.ta.reportportal.core.log.GetLogHandler;
import com.epam.ta.reportportal.core.log.impl.PagedLogResource;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.model.log.GetLogsUnderRq;
import com.epam.ta.reportportal.model.log.LogResource;
import com.epam.ta.reportportal.model.log.SearchLogRq;
import com.epam.ta.reportportal.model.log.SearchLogRs;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.BatchElementCreatedRS;
import com.epam.ta.reportportal.ws.reporting.BatchSaveOperatingRS;
import com.epam.ta.reportportal.ws.reporting.Constants;
import com.epam.ta.reportportal.ws.reporting.EntryCreatedAsyncRS;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v1/{projectName}/log")
@PreAuthorize(ASSIGNED_TO_PROJECT)
@Tag(name = "Log", description = "Logs API collection")
public class LogController {

  private final ProjectExtractor projectExtractor;
  private final CreateLogHandler createLogHandler;
  private final DeleteLogHandler deleteLogHandler;
  private final GetLogHandler getLogHandler;
  private final SearchLogService searchLogService;
  private final Validator validator;

  @Autowired
  public LogController(ProjectExtractor projectExtractor,
      @Autowired CreateLogHandler createLogHandler, DeleteLogHandler deleteLogHandler,
      GetLogHandler getLogHandler, SearchLogService searchLogService, Validator validator) {
    this.projectExtractor = projectExtractor;
    this.createLogHandler = createLogHandler;
    this.deleteLogHandler = deleteLogHandler;
    this.getLogHandler = getLogHandler;
    this.searchLogService = searchLogService;
    this.validator = validator;
  }

  /**
   * @deprecated in favour of {@link LogController#createLogEntry(String, SaveLogRQ,
   * ReportPortalUser)} because of mapping collisions
   */
  /* Report client API */
  @Deprecated
  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(CREATED)
  @Hidden
  @PreAuthorize(ALLOWED_TO_REPORT)
  public EntryCreatedAsyncRS createLog(@PathVariable String projectName,
      @RequestBody SaveLogRQ createLogRQ, @AuthenticationPrincipal ReportPortalUser user) {
    validateSaveRQ(validator, createLogRQ);
    return createLogHandler.createLog(createLogRQ, null,
        projectExtractor.extractProjectDetails(user, projectName)
    );
  }

  /* Report client API */
  @PostMapping(value = "/entry", consumes = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(CREATED)
  @Operation(summary = "Create log")
  @PreAuthorize(ALLOWED_TO_REPORT)
  public EntryCreatedAsyncRS createLogEntry(@PathVariable String projectName,
      @RequestBody SaveLogRQ createLogRQ, @AuthenticationPrincipal ReportPortalUser user) {
    validateSaveRQ(validator, createLogRQ);
    return createLogHandler.createLog(createLogRQ, null,
        projectExtractor.extractProjectDetails(user, projectName)
    );
  }

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  @Operation(summary = "Create log (batching operation)")
  // Specific handler should be added for springfox in case of similar POST
  // request mappings
  //	@Async
  @PreAuthorize(ALLOWED_TO_REPORT)
  public ResponseEntity<BatchSaveOperatingRS> createLog(@PathVariable String projectName,
      @RequestPart(value = Constants.LOG_REQUEST_JSON_PART) SaveLogRQ[] createLogRQs,
      HttpServletRequest request, @AuthenticationPrincipal ReportPortalUser user) {

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
          responseItem = createLog(projectName, createLogRq, user);

        } else {
          /* Find by request part */
          MultipartFile data = findByFileName(filename, uploadedFiles);
          BusinessRule.expect(data, Predicates.notNull())
              .verify(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
                  Suppliers.formattedSupplier("There is no request part or file with name {}",
                      filename
                  )
              );
          /*
           * If provided content type is null or this is octet
           * stream, try to detect real content type of binary
           * data
           */
          //noinspection ConstantConditions
          responseItem = createLogHandler.createLog(createLogRq, data,
              projectExtractor.extractProjectDetails(user, projectName)
          );
        }
        response.addResponse(new BatchElementCreatedRS(responseItem.getId()));
      } catch (Exception e) {
        response.addResponse(new BatchElementCreatedRS(ExceptionUtils.getStackTrace(e),
            ExceptionUtils.getMessage(e)
        ));
      }
    }
    return new ResponseEntity<>(response, CREATED);
  }


  /* Frontend API */

  @RequestMapping(value = "/{logId}", method = RequestMethod.DELETE)
  @Operation(summary = "Delete log")
  @Transactional
  public OperationCompletionRS deleteLog(@PathVariable String projectName, @PathVariable Long logId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteLogHandler.deleteLog(logId,
        projectExtractor.extractProjectDetails(user, projectName), user
    );
  }

  @RequestMapping(method = RequestMethod.GET)
  @Operation(summary = "Get logs by filter")
  @Transactional(readOnly = true)
  public Iterable<LogResource> getLogs(@PathVariable String projectName,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + UNDR + CRITERIA_PATH, required = false)
          String underPath, @FilterFor(Log.class) Filter filter,
      @SortDefault({"logTime"}) @SortFor(Log.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLogHandler.getLogs(underPath,
        projectExtractor.extractProjectDetails(user, projectName), filter, pageable
    );
  }

  @PostMapping(value = "/under")
  @Operation(summary = "Get logs under items")
  @Transactional(readOnly = true)
  public Map<Long, List<LogResource>> getLogsUnder(@PathVariable String projectName,
      @RequestBody GetLogsUnderRq logsUnderRq, @AuthenticationPrincipal ReportPortalUser user) {
    return getLogHandler.getLogs(logsUnderRq,
        projectExtractor.extractProjectDetails(user, projectName)
    );
  }

  @GetMapping(value = "/{logId}/page")
  @Operation(summary = "Get logs by filter")
  @Transactional(readOnly = true)
  public Map<String, Serializable> getPageNumber(@PathVariable String projectName,
      @PathVariable Long logId, @FilterFor(Log.class) Filter filter,
      @SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
    return ImmutableMap.<String, Serializable>builder().put("number",
        getLogHandler.getPageNumber(logId,
            projectExtractor.extractProjectDetails(user, projectName), filter, pageable
        )
    ).build();
  }

  @GetMapping(value = "/{logId}")
  @Operation(summary = "Get log by ID")
  @Transactional(readOnly = true)
  public LogResource getLog(@PathVariable String projectName, @PathVariable String logId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLogHandler.getLog(logId, projectExtractor.extractProjectDetails(user, projectName),
        user
    );
  }

  /**
   * @deprecated Need to remove in version 6.0. This API significantly impacts database performance
   * - high IO operations on getting log record by UUID from production-like database amount.
   */
  @Deprecated(since = "5.8", forRemoval = true)
  @GetMapping(value = "/uuid/{logId}")
  @Operation(summary = "Get log by UUID (Will be removed in version 6.0)")
  @Transactional(readOnly = true)
  public LogResource getLogByUuid(@PathVariable String projectName, @PathVariable String logId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLogHandler.getLog(logId, projectExtractor.extractProjectDetails(user, projectName),
        user
    );
  }

  @GetMapping(value = "/nested/{parentId}")
  @Operation(summary = "Get nested steps with logs for the parent Test Item")
  @Transactional(readOnly = true)
  public Iterable<?> getNestedItems(@PathVariable String projectName, @PathVariable Long parentId,
      @Parameter(required = false) @RequestParam Map<String, String> params,
      @FilterFor(Log.class) Filter filter, @SortFor(Log.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLogHandler.getNestedItems(parentId,
        projectExtractor.extractProjectDetails(user, projectName), params, filter, pageable
    );
  }

  @GetMapping(value = "/locations/{parentId}")
  @Operation(summary = "Get next or previous log in test item")
  @Transactional(readOnly = true)
  public List<PagedLogResource> getErrorPage(@PathVariable String projectName,
      @PathVariable Long parentId, @RequestParam Map<String, String> params,
      @FilterFor(Log.class) Filter filter, @SortFor(Log.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLogHandler.getLogsWithLocation(parentId,
        projectExtractor.extractProjectDetails(user, projectName), params, filter, pageable
    );
  }

  @PostMapping("search/{itemId}")
  @ResponseStatus(OK)
  @Operation(summary = "Search test items with similar error logs")
  public Iterable<SearchLogRs> searchLogs(@PathVariable String projectName,
      @RequestBody SearchLogRq request, @PathVariable Long itemId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return searchLogService.search(itemId, request,
        projectExtractor.extractProjectDetails(user, projectName)
    );
  }

}
