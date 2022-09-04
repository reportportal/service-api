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

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.auto.SearchLogService;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.core.log.DeleteLogHandler;
import com.epam.ta.reportportal.core.log.GetLogHandler;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.log.*;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.querygen.Condition.UNDR;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.util.ControllerUtils.*;
import static com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver.DEFAULT_FILTER_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v1/{projectKey}/log")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class LogController {

	private final ProjectExtractor projectExtractor;
	private final CreateLogHandler createLogHandler;
	private final DeleteLogHandler deleteLogHandler;
	private final GetLogHandler getLogHandler;
	private final SearchLogService searchLogService;
	private final Validator validator;

	@Autowired
	public LogController(ProjectExtractor projectExtractor, @Autowired CreateLogHandler createLogHandler, DeleteLogHandler deleteLogHandler, GetLogHandler getLogHandler,
			SearchLogService searchLogService, Validator validator) {
		this.projectExtractor = projectExtractor;
		this.createLogHandler = createLogHandler;
		this.deleteLogHandler = deleteLogHandler;
		this.getLogHandler = getLogHandler;
		this.searchLogService = searchLogService;
		this.validator = validator;
	}

	/**
	 * @deprecated in favour of {@link LogController#createLogEntry(String, SaveLogRQ, ReportPortalUser)} because of mapping collisions
	 */
	/* Report client API */
	@Deprecated
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(CREATED)
	@ApiIgnore
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS createLog(@PathVariable String projectKey, @RequestBody SaveLogRQ createLogRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		validateSaveRQ(validator, createLogRQ);
		return createLogHandler.createLog(createLogRQ, null, projectExtractor.extractProjectDetails(user, projectKey));
	}

	/* Report client API */
	@PostMapping(value = "/entry", consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(CREATED)
	@ApiOperation("Create log")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS createLogEntry(@PathVariable String projectKey, @RequestBody SaveLogRQ createLogRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		validateSaveRQ(validator, createLogRQ);
		return createLogHandler.createLog(createLogRQ, null, projectExtractor.extractProjectDetails(user, projectKey));
	}

	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ApiOperation("Create log (batching operation)")
	// Specific handler should be added for springfox in case of similar POST
	// request mappings
	//	@Async
	@PreAuthorize(ALLOWED_TO_REPORT)
	public ResponseEntity<BatchSaveOperatingRS> createLog(@PathVariable String projectKey,
			@RequestPart(value = Constants.LOG_REQUEST_JSON_PART) SaveLogRQ[] createLogRQs, HttpServletRequest request,
			@AuthenticationPrincipal ReportPortalUser user) {

		/*
		 * Since this is multipart request we can retrieve list of uploaded
		 * attachments
		 */
		Map<String, MultipartFile> uploadedFiles = getUploadedFiles(request);
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
					BusinessRule.expect(data, Predicates.notNull()).verify(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
							Suppliers.formattedSupplier("There is no request part or file with name {}", filename)
					);
					/*
					 * If provided content type is null or this is octet
					 * stream, try to detect real content type of binary
					 * data
					 */
					//noinspection ConstantConditions
					responseItem = createLogHandler.createLog(createLogRq, data, projectExtractor.extractProjectDetails(user, projectKey));
				}
				response.addResponse(new BatchElementCreatedRS(responseItem.getId()));
			} catch (Exception e) {
				response.addResponse(new BatchElementCreatedRS(ExceptionUtils.getStackTrace(e), ExceptionUtils.getMessage(e)));
			}
		}
		return new ResponseEntity<>(response, CREATED);
	}


	/* Frontend API */

	@RequestMapping(value = "/{logId}", method = RequestMethod.DELETE)
	@ApiOperation("Delete log")
	@Transactional
	public OperationCompletionRS deleteLog(@PathVariable String projectKey, @PathVariable Long logId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLogHandler.deleteLog(logId, projectExtractor.extractProjectDetails(user, projectKey), user);
	}

	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation("Get logs by filter")
	@Transactional(readOnly = true)
	public Iterable<LogResource> getLogs(@PathVariable String projectKey,
			@RequestParam(value = DEFAULT_FILTER_PREFIX + UNDR + CRITERIA_PATH, required = false) String underPath,
			@FilterFor(Log.class) Filter filter,
			@SortDefault({ "logTime" }) @SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLogs(underPath, projectExtractor.extractProjectDetails(user, projectKey), filter, pageable);
	}

	@PostMapping(value = "/under")
	@ApiOperation("Get logs under items")
	@Transactional(readOnly = true)
	public Map<Long, List<LogResource>> getLogsUnder(@PathVariable String projectKey,
			@RequestBody GetLogsUnderRq logsUnderRq, @AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLogs(logsUnderRq, projectExtractor.extractProjectDetails(user, projectKey));
	}

	@GetMapping(value = "/{logId}/page")
	@ApiOperation("Get logs by filter")
	@Transactional(readOnly = true)
	public Map<String, Serializable> getPageNumber(@PathVariable String projectKey, @PathVariable Long logId,
			@FilterFor(Log.class) Filter filter, @SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return ImmutableMap.<String, Serializable>builder().put("number",
				getLogHandler.getPageNumber(logId, projectExtractor.extractProjectDetails(user, projectKey), filter, pageable)
		).build();
	}

	@GetMapping(value = "/{logId}")
	@ApiOperation("Get log by ID")
	@Transactional(readOnly = true)
	public LogResource getLog(@PathVariable String projectKey, @PathVariable String logId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLog(logId, projectExtractor.extractProjectDetails(user, projectKey), user);
	}

	@GetMapping(value = "/uuid/{logId}")
	@ApiOperation("Get log by UUID")
	@Transactional(readOnly = true)
	public LogResource getLogByUuid(@PathVariable String projectKey, @PathVariable String logId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLog(logId, projectExtractor.extractProjectDetails(user, projectKey), user);
	}

	@GetMapping(value = "/nested/{parentId}")
	@ApiOperation("Get nested steps with logs for the parent Test Item")
	@Transactional(readOnly = true)
	public Iterable<?> getNestedItems(@PathVariable String projectKey, @PathVariable Long parentId,
			@ApiParam(required = false) @RequestParam Map<String, String> params, @FilterFor(Log.class) Filter filter,
			@SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getNestedItems(parentId, projectExtractor.extractProjectDetails(user, projectKey), params, filter, pageable);
	}

	@PostMapping("search/{itemId}")
	@ResponseStatus(OK)
	@ApiOperation("Search test items with similar error logs")
	public Iterable<SearchLogRs> searchLogs(@PathVariable String projectKey, @RequestBody SearchLogRq request, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return searchLogService.search(itemId, request, projectExtractor.extractProjectDetails(user, projectKey));
	}

}
