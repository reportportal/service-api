/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.core.log.IDeleteLogHandler;
import com.epam.ta.reportportal.core.log.IGetLogHandler;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.controller.ILogController;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Path.Node;
import javax.validation.Validator;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.CREATED;

/**
 * Log Controller implementation
 *
 * @author Andrei Varabyeu
 */
@Controller
@RequestMapping("/{projectName}/log")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class LogController implements ILogController {

	private final ICreateLogHandler createLogMessageHandler;
	private final IDeleteLogHandler deleteLogMessageHandler;
	private final IGetLogHandler getLogHandler;
	private final Validator validator;

	@Autowired
	public LogController(ICreateLogHandler createLogMessageHandler, IDeleteLogHandler deleteLogMessageHandler, IGetLogHandler getLogHandler,
			Validator validator) {
		this.createLogMessageHandler = createLogMessageHandler;
		this.deleteLogMessageHandler = deleteLogMessageHandler;
		this.getLogHandler = getLogHandler;
		this.validator = validator;
	}

	@Override
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Create log")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS createLog(@PathVariable String projectName, @RequestBody SaveLogRQ createLogRQ, Principal principal) {
		validateSaveRQ(createLogRQ);
		return createLogMessageHandler.createLog(createLogRQ, null, EntityUtils.normalizeId(projectName));
	}

	@Override
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	// @ApiOperation("Create log (batching operation)")
	// Specific handler should be added for springfox in case of similar POST
	// request mappings
	@ApiIgnore
	@Async
	@PreAuthorize(ALLOWED_TO_REPORT)
	public ResponseEntity<BatchSaveOperatingRS> createLog(@PathVariable String projectName,
			@RequestPart(value = Constants.LOG_REQUEST_JSON_PART) SaveLogRQ[] createLogRQs, HttpServletRequest request,
			Principal principal) {

		String prjName = EntityUtils.normalizeId(projectName);
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
					responseItem = createLog(prjName, createLogRq, principal);

				} else {
					/* Find by request part */
					MultipartFile data = findByFileName(filename, uploadedFiles);
					BusinessRule.expect(data, Predicates.notNull())
							.verify(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
									Suppliers.formattedSupplier("There is no request part or file with name {}", filename)
							);
					/*
					 * If provided content type is null or this is octet
					 * stream, try to detect real content type of binary
					 * data
					 */
					//noinspection ConstantConditions
					responseItem = createLogMessageHandler.createLog(createLogRq, data, prjName);
				}
				response.addResponse(new BatchElementCreatedRS(responseItem.getId()));
			} catch (Exception e) {
				response.addResponse(new BatchElementCreatedRS(ExceptionUtils.getStackTrace(e), ExceptionUtils.getMessage(e)));
			}
		}
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Override
	@RequestMapping(value = "/{logId}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation("Delete log")
	public OperationCompletionRS deleteLog(@PathVariable String projectName, @PathVariable String logId, Principal principal) {
		return deleteLogMessageHandler.deleteLog(logId, EntityUtils.normalizeId(projectName), principal.getName());
	}

	@Override
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation("Get logs by filter")
	public Iterable<LogResource> getLogs(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.EQ + Log.TEST_ITEM_ID) String testStepId,
			@FilterFor(Log.class) Filter filter, @SortDefault({ "time" }) @SortFor(Log.class) Pageable pageable, Principal principal) {
		return getLogHandler.getLogs(testStepId, EntityUtils.normalizeId(projectName), filter, pageable);
	}

	@Override
	@RequestMapping(value = "/{logId}/page", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation("Get logs by filter")
	public Map<String, Serializable> getPageNumber(@PathVariable String projectName, @PathVariable String logId,
			@FilterFor(Log.class) Filter filter, @SortFor(Log.class) Pageable pageable, Principal principal) {
		return ImmutableMap.<String, Serializable>builder().put(
				"number", getLogHandler.getPageNumber(logId, EntityUtils.normalizeId(projectName), filter, pageable)).build();
	}

	@Override
	@RequestMapping(value = "/{logId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation("Get log")
	public LogResource getLog(@PathVariable String projectName, @PathVariable String logId, Principal principal) {
		return getLogHandler.getLog(logId, EntityUtils.normalizeId(projectName));
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
				Iterator<Node> iterator = constraintViolation.getPropertyPath().iterator();
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
