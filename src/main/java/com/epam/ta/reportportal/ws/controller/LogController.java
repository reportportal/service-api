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
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.epam.ta.reportportal.ws.model.log.SearchLogRq;
import com.epam.ta.reportportal.ws.model.log.SearchLogRs;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.io.Serializable;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.querygen.Condition.UNDR;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.util.ControllerUtils.*;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver.DEFAULT_FILTER_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v1/{projectName}/log")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class LogController {

	private final CreateLogHandler createLogHandler;
	private final DeleteLogHandler deleteLogHandler;
	private final GetLogHandler getLogHandler;
	private final SearchLogService searchLogService;
	private final Validator validator;

	@Autowired
	public LogController(@Autowired CreateLogHandler createLogHandler, DeleteLogHandler deleteLogHandler, GetLogHandler getLogHandler,
			SearchLogService searchLogService, Validator validator) {
		this.createLogHandler = createLogHandler;
		this.deleteLogHandler = deleteLogHandler;
		this.getLogHandler = getLogHandler;
		this.searchLogService = searchLogService;
		this.validator = validator;
	}


	/* Report client API */

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(CREATED)
	@ApiOperation("Create log")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS createLog(@PathVariable String projectName, @RequestBody SaveLogRQ createLogRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		validateSaveRQ(validator, createLogRQ);
		return createLogHandler.createLog(createLogRQ, null, extractProjectDetails(user, projectName));
	}

	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	// @ApiOperation("Create log (batching operation)")
	// Specific handler should be added for springfox in case of similar POST
	// request mappings
	//	@Async
	@PreAuthorize(ALLOWED_TO_REPORT)
	public ResponseEntity<BatchSaveOperatingRS> createLog(@PathVariable String projectName,
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
					responseItem = createLog(projectName, createLogRq, user);

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
					responseItem = createLogHandler.createLog(createLogRq, data, extractProjectDetails(user, projectName));
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
	public OperationCompletionRS deleteLog(@PathVariable String projectName, @PathVariable Long logId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLogHandler.deleteLog(logId, extractProjectDetails(user, projectName), user);
	}

	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation("Get logs by filter")
	@Transactional(readOnly = true)
	public Iterable<LogResource> getLogs(@PathVariable String projectName,
			@RequestParam(value = DEFAULT_FILTER_PREFIX + UNDR + CRITERIA_PATH, required = false) String underPath,
			@FilterFor(Log.class) Filter filter,
			@SortDefault({ "logTime" }) @SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLogs(underPath, extractProjectDetails(user, projectName), filter, pageable);
	}

	@GetMapping(value = "/{logId}/page")
	@ApiOperation("Get logs by filter")
	@Transactional(readOnly = true)
	public Map<String, Serializable> getPageNumber(@PathVariable String projectName, @PathVariable Long logId,
			@FilterFor(Log.class) Filter filter, @SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return ImmutableMap.<String, Serializable>builder().put("number",
				getLogHandler.getPageNumber(logId, extractProjectDetails(user, projectName), filter, pageable)
		).build();
	}

	@GetMapping(value = "/{logId}")
	@ApiOperation("Get log by ID")
	@Transactional(readOnly = true)
	public LogResource getLog(@PathVariable String projectName, @PathVariable String logId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLog(logId, extractProjectDetails(user, projectName), user);
	}

	@GetMapping(value = "/uuid/{logId}")
	@ApiOperation("Get log by UUID")
	@Transactional(readOnly = true)
	public LogResource getLogByUuid(@PathVariable String projectName, @PathVariable String logId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLog(logId, extractProjectDetails(user, projectName), user);
	}

	@GetMapping(value = "/nested/{parentId}")
	@ApiOperation("Get nested steps with logs for the parent Test Item")
	@Transactional(readOnly = true)
	public Iterable<?> getNestedItems(@PathVariable String projectName, @PathVariable Long parentId,
			@ApiParam(required = false) @RequestParam Map<String, String> params, @FilterFor(Log.class) Filter filter,
			@SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getNestedItems(parentId, extractProjectDetails(user, projectName), params, filter, pageable);
	}

	@PostMapping("search/{itemId}")
	@ResponseStatus(OK)
	@ApiOperation("Search test items with similar error logs")
	public Iterable<SearchLogRs> searchLogs(@PathVariable String projectName, @RequestBody SearchLogRq request, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return searchLogService.search(itemId, request, extractProjectDetails(user, projectName));
	}

}
