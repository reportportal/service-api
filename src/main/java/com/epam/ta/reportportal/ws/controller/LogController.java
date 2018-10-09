/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.annotation.Regular;
import com.epam.ta.reportportal.core.log.impl.CreateLogHandler;
import com.epam.ta.reportportal.core.log.impl.DeleteLogHandler;
import com.epam.ta.reportportal.core.log.impl.GetLogHandler;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.io.Serializable;
import java.util.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.TEST_ITEM_ID;
import static com.epam.ta.reportportal.util.ProjectUtils.extractProjectDetails;
import static org.springframework.http.HttpStatus.CREATED;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/{projectName}/log")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class LogController {

	private final CreateLogHandler createLogMessageHandler;
	private final DeleteLogHandler deleteLogHandler;
	private final GetLogHandler getLogHandler;
	private final Validator validator;

	@Autowired
	public LogController(@Regular CreateLogHandler createLogMessageHandler, DeleteLogHandler deleteLogHandler, GetLogHandler getLogHandler,
			Validator validator) {
		this.createLogMessageHandler = createLogMessageHandler;
		this.deleteLogHandler = deleteLogHandler;
		this.getLogHandler = getLogHandler;
		this.validator = validator;
	}

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(CREATED)
	@ApiOperation("Create log")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS createLog(@PathVariable String projectName, @RequestBody SaveLogRQ createLogRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		validateSaveRQ(createLogRQ);
		return createLogMessageHandler.createLog(createLogRQ, null, extractProjectDetails(user, projectName));
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
		EntryCreatedRS responseItem;
		/* Go through all provided save log request items */
		for (SaveLogRQ createLogRq : createLogRQs) {
			try {
				validateSaveRQ(createLogRq);
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
					responseItem = createLogMessageHandler.createLog(createLogRq, data, extractProjectDetails(user, projectName));
				}
				response.addResponse(new BatchElementCreatedRS(responseItem.getId()));
			} catch (Exception e) {
				response.addResponse(new BatchElementCreatedRS(ExceptionUtils.getStackTrace(e), ExceptionUtils.getMessage(e)));
			}
		}
		return new ResponseEntity<>(response, CREATED);
	}

	@RequestMapping(value = "/{logId}", method = RequestMethod.DELETE)
	@ApiOperation("Delete log")
	public OperationCompletionRS deleteLog(@PathVariable String projectName, @PathVariable Long logId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLogHandler.deleteLog(logId, extractProjectDetails(user, projectName), user);
	}

	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation("Get logs by filter")
	public Iterable<LogResource> getLogs(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.EQ + TEST_ITEM_ID) Long testStepId,
			@FilterFor(Log.class) Filter filter, @SortDefault({ "time" }) @SortFor(Log.class) Pageable pageable,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLogs(testStepId, extractProjectDetails(user, projectName), filter, pageable);
	}

	@GetMapping(value = "/{logId}/page")
	@ApiOperation("Get logs by filter")
	public Map<String, Serializable> getPageNumber(@PathVariable String projectName, @PathVariable Long logId,
			@FilterFor(Log.class) Filter filter, @SortFor(Log.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return ImmutableMap.<String, Serializable>builder().put("number",
				getLogHandler.getPageNumber(logId, extractProjectDetails(user, projectName), filter, pageable)
		).build();
	}

	@GetMapping(value = "/{logId}")
	@ApiOperation("Get log")
	public LogResource getLog(@PathVariable String projectName, @PathVariable Long logId, @AuthenticationPrincipal ReportPortalUser user) {
		return getLogHandler.getLog(logId, extractProjectDetails(user, projectName), user);
	}

	/**
	 * Tries to find request part or file with specified name in multipart attachments
	 * map.
	 *
	 * @param filename File name
	 * @param files    Files map
	 * @return Found file
	 */
	private MultipartFile findByFileName(String filename, Map<String, MultipartFile> files) {
		/* Request part name? */
		if (files.containsKey(filename)) {
			return files.get(filename);
		}
		/* Filename? */
		for (MultipartFile file : files.values()) {
			if (filename.equals(file.getOriginalFilename())) {
				return file;
			}
		}
		return null;
	}

	private void validateSaveRQ(SaveLogRQ saveLogRQ) {
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

	private Map<String, MultipartFile> getUploadedFiles(HttpServletRequest request) {
		Map<String, MultipartFile> uploadedFiles = new HashMap<>();
		if (request instanceof MultipartHttpServletRequest) {
			MultiValueMap<String, MultipartFile> multiFileMap = (((MultipartHttpServletRequest) request)).getMultiFileMap();
			for (List<MultipartFile> multipartFiles : multiFileMap.values()) {
				for (MultipartFile file : multipartFiles) {
					uploadedFiles.put(file.getOriginalFilename(), file);
				}
			}
		}
		return uploadedFiles;
	}
}
