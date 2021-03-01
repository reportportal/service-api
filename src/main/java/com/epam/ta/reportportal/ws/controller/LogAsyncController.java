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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.core.logging.HttpLogging;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.util.ControllerUtils.*;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.springframework.http.HttpStatus.CREATED;

/**
 * @author Konstantin Antipin
 */
@RestController
@RequestMapping("/v2/{projectName}/log")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class LogAsyncController {

	private final CreateLogHandler createLogHandler;
	private final Validator validator;

	public LogAsyncController(@Autowired @Qualifier("asyncCreateLogHandler") CreateLogHandler createLogHandler, Validator validator) {
		this.createLogHandler = createLogHandler;
		this.validator = validator;
	}

	/**
	 * @deprecated in favour of {@link LogAsyncController#createLogEntry(String, SaveLogRQ, ReportPortalUser)} because of mapping collisions
	 */
	/* Report client API */
	@Deprecated
	@HttpLogging
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(CREATED)
	@ApiIgnore
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS createLog(@PathVariable String projectName, @RequestBody SaveLogRQ createLogRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		validateSaveRQ(validator, createLogRQ);
		return createLogHandler.createLog(createLogRQ, null, extractProjectDetails(user, projectName));
	}

	@HttpLogging
	@PostMapping(value = "/entry", consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(CREATED)
	@ApiOperation("Create log")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS createLogEntry(@PathVariable String projectName, @RequestBody SaveLogRQ createLogRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		validateSaveRQ(validator, createLogRQ);
		return createLogHandler.createLog(createLogRQ, null, extractProjectDetails(user, projectName));
	}

	@HttpLogging
	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ApiOperation("Create log (batching operation)")
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
					BusinessRule.expect(data, Predicates.notNull()).verify(
							ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
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
}
