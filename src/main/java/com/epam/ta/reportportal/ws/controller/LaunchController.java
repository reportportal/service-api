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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.imprt.ImportLaunchHandler;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.launch.*;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.launch.*;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.net.HttpHeaders;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER_OR_ADMIN;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.composeBaseUrl;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Controller implementation for
 * {@link com.epam.ta.reportportal.entity.launch.Launch} entity
 * <p>
 * Note: please use EntityUtils for forced lower case for user names and project
 * names
 * </p>
 *
 * @author Andrei Varabyeu
 * @author Andrei Kliashchonak
 * @author Andrei_Ramanchuk
 */
@RestController
@RequestMapping("/v1/{projectName}/launch")
public class LaunchController {

	private final StartLaunchHandler startLaunchHandler;
	private final FinishLaunchHandler finishLaunchHandler;
	private final StopLaunchHandler stopLaunchHandler;
	private final DeleteLaunchHandler deleteLaunchMessageHandler;
	private final GetLaunchHandler getLaunchMessageHandler;
	private final UpdateLaunchHandler updateLaunchHandler;
	private final MergeLaunchHandler mergeLaunchesHandler;
	private final ImportLaunchHandler importLaunchHandler;
	private final GetJasperReportHandler<Launch> getJasperHandler;

	public LaunchController(StartLaunchHandler startLaunchHandler, FinishLaunchHandler finishLaunchHandler,
			StopLaunchHandler stopLaunchHandler, DeleteLaunchHandler deleteLaunchMessageHandler, GetLaunchHandler getLaunchMessageHandler,
			UpdateLaunchHandler updateLaunchHandler, MergeLaunchHandler mergeLaunchesHandler, ImportLaunchHandler importLaunchHandler,
			@Qualifier("launchJasperReportHandler") GetJasperReportHandler<Launch> getJasperHandler) {
		this.startLaunchHandler = startLaunchHandler;
		this.finishLaunchHandler = finishLaunchHandler;
		this.stopLaunchHandler = stopLaunchHandler;
		this.deleteLaunchMessageHandler = deleteLaunchMessageHandler;
		this.getLaunchMessageHandler = getLaunchMessageHandler;
		this.updateLaunchHandler = updateLaunchHandler;
		this.mergeLaunchesHandler = mergeLaunchesHandler;
		this.importLaunchHandler = importLaunchHandler;
		this.getJasperHandler = getJasperHandler;
	}

	/* Report client API */

	@PostMapping
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(CREATED)
	@ApiOperation("Starts launch for specified project")
	public StartLaunchRS startLaunch(@PathVariable String projectName,
			@ApiParam(value = "Start launch request body", required = true) @RequestBody @Validated @Valid StartLaunchRQ startLaunchRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return startLaunchHandler.startLaunch(user, extractProjectDetails(user, normalizeId(projectName)), startLaunchRQ);
	}

	@PutMapping(value = "/{launchId}/finish")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Finish launch for specified project")
	public FinishLaunchRS finishLaunch(@PathVariable String projectName, @PathVariable String launchId,
			@RequestBody @Validated FinishExecutionRQ finishLaunchRQ, @AuthenticationPrincipal ReportPortalUser user,
			HttpServletRequest request) {
		return finishLaunchHandler.finishLaunch(launchId,
				finishLaunchRQ,
				extractProjectDetails(user, normalizeId(projectName)),
				user,
				composeBaseUrl(request)
		);
	}

	/* Frontend API */

	@Transactional
	@PutMapping("/{launchId}/stop")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch for specified project")
	public OperationCompletionRS forceFinishLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated FinishExecutionRQ finishExecutionRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return stopLaunchHandler.stopLaunch(launchId, finishExecutionRQ, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional
	@PutMapping("/stop")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch")
	public List<OperationCompletionRS> bulkForceFinish(@PathVariable String projectName,
			@RequestBody @Validated BulkRQ<Long, FinishExecutionRQ> rq, @AuthenticationPrincipal ReportPortalUser user) {
		return stopLaunchHandler.stopLaunch(rq, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional
	@PutMapping("/{launchId}/update")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Updates launch for specified project")
	public OperationCompletionRS updateLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated UpdateLaunchRQ updateLaunchRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.updateLaunch(launchId, extractProjectDetails(user, normalizeId(projectName)), user, updateLaunchRQ);
	}

	@Transactional
	@PutMapping("/update")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Updates launches for specified project")
	public List<OperationCompletionRS> updateLaunches(@PathVariable String projectName,
			@RequestBody @Validated BulkRQ<Long, UpdateLaunchRQ> rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.updateLaunch(rq, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional
	@DeleteMapping("/{launchId}")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launch by ID")
	public OperationCompletionRS deleteLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLaunchMessageHandler.deleteLaunch(launchId, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{launchId}")
	@ResponseStatus(OK)
	@ApiOperation("Get specified launch by ID")
	public LaunchResource getLaunch(@PathVariable String projectName, @PathVariable String launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunch(launchId, extractProjectDetails(user, normalizeId(projectName)));
	}

	@Transactional(readOnly = true)
	@GetMapping("/uuid/{launchId}")
	@ResponseStatus(OK)
	@ApiOperation("Get specified launch by UUID")
	public LaunchResource getLaunchByUuid(@PathVariable String projectName, @PathVariable String launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunch(launchId, extractProjectDetails(user, normalizeId(projectName)));
	}

	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(OK)
	@ApiOperation("Get list of project launches by filter")
	public Iterable<LaunchResource> getProjectLaunches(@PathVariable String projectName, @FilterFor(Launch.class) Filter filter,
			@SortFor(Launch.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getProjectLaunches(extractProjectDetails(user, normalizeId(projectName)),
				filter,
				pageable,
				user.getUsername()
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/latest")
	@ResponseStatus(OK)
	@ApiOperation("Get list of latest project launches by filter")
	public Page<LaunchResource> getLatestLaunches(@PathVariable String projectName, @FilterFor(Launch.class) Filter filter,
			@SortFor(Launch.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLatestLaunches(extractProjectDetails(user, normalizeId(projectName)), filter, pageable);
	}

	@GetMapping(value = "/mode")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get launches of specified project from DEBUG mode")
	public Iterable<LaunchResource> getDebugLaunches(@PathVariable String projectName, @FilterFor(Launch.class) Filter filter,
			@SortFor(Launch.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getDebugLaunches(extractProjectDetails(user, normalizeId(projectName)), filter, pageable);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/attribute/keys")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique attribute keys of project launches")
	public List<String> getAttributeKeys(@PathVariable String projectName,
			@RequestParam(value = "filter." + "cnt." + "attributeKey") String value, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getAttributeKeys(extractProjectDetails(user, normalizeId(projectName)), value);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/attribute/values")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique attribute values of project launches")
	public List<String> getAttributeValues(@PathVariable String projectName,
			@RequestParam(value = "filter." + "eq." + "attributeKey", required = false) String key,
			@RequestParam(value = "filter." + "cnt." + "attributeValue") String value, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getAttributeValues(extractProjectDetails(user, normalizeId(projectName)), key, value);
	}

	@Transactional
	@PutMapping(value = "/info")
	@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ResponseStatus(OK)
	@ApiOperation("Bulk update attributes and description")
	public OperationCompletionRS bulkUpdate(@PathVariable String projectName, @RequestBody @Validated BulkInfoUpdateRQ bulkInfoUpdateRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.bulkInfoUpdate(bulkInfoUpdateRQ, extractProjectDetails(user, projectName));
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/owners")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique owners of project launches")
	public List<String> getAllOwners(@PathVariable String projectName, @RequestParam(value = "filter." + "cnt." + "user") String value,
			@RequestParam(value = "mode", required = false, defaultValue = "DEFAULT") String mode,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getOwners(extractProjectDetails(user, normalizeId(projectName)), value, mode);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/names")
	@ResponseStatus(OK)
	@ApiOperation("Get launch names of project")
	public List<String> getAllLaunchNames(@PathVariable String projectName, @RequestParam(value = "filter." + "cnt." + "name") String value,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunchNames(extractProjectDetails(user, normalizeId(projectName)), value);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/compare")
	@ResponseStatus(OK)
	@ApiOperation("Compare launches")
	public Map<String, List<ChartStatisticsContent>> compareLaunches(@PathVariable String projectName,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunchesComparisonInfo(extractProjectDetails(user, normalizeId(projectName)), ids);
	}

	@Transactional
	@PostMapping("/merge")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Merge set of specified launches in common one")
	public LaunchResource mergeLaunches(@PathVariable String projectName,
			@ApiParam(value = "Merge launches request body", required = true) @RequestBody @Validated MergeLaunchesRQ mergeLaunchesRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return mergeLaunchesHandler.mergeLaunches(extractProjectDetails(user, normalizeId(projectName)), user, mergeLaunchesRQ);
	}

	@Transactional
	@PostMapping(value = "/analyze")
	@ResponseStatus(OK)
	@ApiOperation("Start launch auto-analyzer on demand")
	public OperationCompletionRS startLaunchAnalyzer(@PathVariable String projectName,
			@RequestBody @Validated AnalyzeLaunchRQ analyzeLaunchRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.startLaunchAnalyzer(analyzeLaunchRQ, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/status")
	@ResponseStatus(OK)

	public Map<String, String> getStatuses(@PathVariable String projectName, @RequestParam(value = "ids") Long[] ids,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getStatuses(extractProjectDetails(user, normalizeId(projectName)), ids);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{launchId}/report")
	@ResponseStatus(OK)
	@ApiOperation(value = "Export specified launch", notes = "Only following formats are supported: pdf (by default), xls, html.")
	public void getLaunchReport(@PathVariable String projectName, @PathVariable Long launchId,
			@ApiParam(allowableValues = "pdf, xls, html") @RequestParam(value = "view", required = false, defaultValue = "pdf") String view,
			@AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {

		ReportFormat format = getJasperHandler.getReportFormat(view);
		response.setContentType(format.getContentType());

		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
				String.format("attachment; filename=RP_LAUNCH_%s_Report.%s", format.name(), format.getValue())
		);

		try (OutputStream outputStream = response.getOutputStream()) {
			getLaunchMessageHandler.exportLaunch(launchId, format, outputStream, user);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unable to write data to the response.");
		}
	}

	@Transactional
	@DeleteMapping
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launches by ids")
	public DeleteBulkRS deleteLaunches(@PathVariable String projectName, @RequestBody @Valid DeleteBulkRQ deleteBulkRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLaunchMessageHandler.deleteLaunches(deleteBulkRQ, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseStatus(OK)
	@ApiOperation(value = "Import junit xml report", notes = "Only following formats are supported: zip.")
	public OperationCompletionRS importLaunch(@PathVariable String projectName, @RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal ReportPortalUser user, HttpServletRequest request) {
		return importLaunchHandler.importLaunch(extractProjectDetails(user, normalizeId(projectName)),
				user,
				"XUNIT",
				file,
				composeBaseUrl(request)
		);
	}
}
