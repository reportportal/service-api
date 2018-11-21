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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.imprt.ImportLaunchHandler;
import com.epam.ta.reportportal.core.jasper.IGetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.ReportFormat;
import com.epam.ta.reportportal.core.launch.*;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.content.LaunchesStatisticsContent;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.launch.*;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.net.HttpHeaders;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
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
@RequestMapping("/{projectName}/launch")
@PreAuthorize(ALLOWED_TO_REPORT)
public class LaunchController {

	private final StartLaunchHandler createLaunchMessageHandler;
	private final FinishLaunchHandler finishLaunchMessageHandler;
	private final DeleteLaunchHandler deleteLaunchMessageHandler;
	private final GetLaunchHandler getLaunchMessageHandler;
	private final UpdateLaunchHandler updateLaunchHandler;
	private final MergeLaunchHandler mergeLaunchesHandler;
	private final ImportLaunchHandler importLaunchHandler;
	private final IGetJasperReportHandler getJasperHandler;

	@Autowired
	public LaunchController(StartLaunchHandler createLaunchMessageHandler, FinishLaunchHandler finishLaunchMessageHandler,
			DeleteLaunchHandler deleteLaunchMessageHandler, GetLaunchHandler getLaunchMessageHandler,
			UpdateLaunchHandler updateLaunchHandler, MergeLaunchHandler mergeLaunchesHandler, ImportLaunchHandler importLaunchHandler,
			IGetJasperReportHandler getJasperHandler) {
		this.createLaunchMessageHandler = createLaunchMessageHandler;
		this.finishLaunchMessageHandler = finishLaunchMessageHandler;
		this.deleteLaunchMessageHandler = deleteLaunchMessageHandler;
		this.getLaunchMessageHandler = getLaunchMessageHandler;
		this.updateLaunchHandler = updateLaunchHandler;
		this.mergeLaunchesHandler = mergeLaunchesHandler;
		this.importLaunchHandler = importLaunchHandler;
		this.getJasperHandler = getJasperHandler;
	}

	@Transactional
	@PostMapping
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(CREATED)
	@ApiOperation("Starts launch for specified project")
	public EntryCreatedRS startLaunch(HttpServletRequest request, @PathVariable String projectName,
			@ApiParam(value = "Start launch request body", required = true) @RequestBody @Validated StartLaunchRQ startLaunchRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		LaunchWithLinkRS rs = createLaunchMessageHandler.startLaunch(user,
				extractProjectDetails(user, normalizeId(projectName)),
				startLaunchRQ
		);

		rs.setLink(generateLaunchLink(request, projectName, String.valueOf(rs.getId())));
		return rs;
	}

	@Transactional
	@PutMapping(value = "/{launchId}/finish")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Finish launch for specified project")
	public LaunchWithLinkRS finishLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated FinishExecutionRQ finishLaunchRQ, @AuthenticationPrincipal ReportPortalUser user,
			HttpServletRequest request) {
		LaunchWithLinkRS rs = finishLaunchMessageHandler.finishLaunch(
				launchId,
				finishLaunchRQ,
				extractProjectDetails(user, normalizeId(projectName)),
				user
		);
		rs.setLink(generateLaunchLink(request, projectName, String.valueOf(rs.getId())));
		return rs;
	}

	@Transactional
	@PutMapping("/{launchId}/stop")
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch for specified project")
	public OperationCompletionRS forceFinishLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated FinishExecutionRQ finishExecutionRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return finishLaunchMessageHandler.stopLaunch(launchId,
				finishExecutionRQ,
				extractProjectDetails(user, normalizeId(projectName)),
				user
		);
	}

	@Transactional
	@PutMapping("/stop")
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch")
	public List<OperationCompletionRS> bulkForceFinish(@PathVariable String projectName,
			@RequestBody @Validated BulkRQ<FinishExecutionRQ> rq, @AuthenticationPrincipal ReportPortalUser user) {
		return finishLaunchMessageHandler.stopLaunch(rq, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional
	@PutMapping("/{launchId}/update")
	@ResponseStatus(OK)
	@ApiOperation("Updates launch for specified project")
	public OperationCompletionRS updateLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated UpdateLaunchRQ updateLaunchRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.updateLaunch(launchId, extractProjectDetails(user, normalizeId(projectName)), user, updateLaunchRQ);
	}

	@Transactional
	@PutMapping("/update")
	@ResponseStatus(OK)
	@ApiOperation("Updates launches for specified project")
	public List<OperationCompletionRS> updateLaunches(@PathVariable String projectName, @RequestBody @Validated BulkRQ<UpdateLaunchRQ> rq,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.updateLaunch(rq, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional
	@DeleteMapping("/{launchId}")
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launch by ID")
	public OperationCompletionRS deleteLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLaunchMessageHandler.deleteLaunch(launchId, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{launchId}")
	@ResponseStatus(OK)
	@ApiOperation("Get specified launch")
	public LaunchResource getLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunch(launchId, extractProjectDetails(user, normalizeId(projectName)), user.getUsername());
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
	@GetMapping(value = "/tags")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique tags of project launches")
	public List<String> getAllTags(@PathVariable String projectName, @RequestParam(value = "filter." + "cnt." + "tags") String value,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getTags(extractProjectDetails(user, normalizeId(projectName)), value);
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
	public Map<String, List<LaunchesStatisticsContent>> compareLaunches(@PathVariable String projectName,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunchesComparisonInfo(extractProjectDetails(user, normalizeId(projectName)), ids);
	}

	@Transactional
	@PostMapping("/merge")
	@ResponseStatus(OK)
	@ApiOperation("Merge set of specified launches in common one")
	public LaunchResource mergeLaunches(@PathVariable String projectName,
			@ApiParam(value = "Merge launches request body", required = true) @RequestBody @Validated MergeLaunchesRQ mergeLaunchesRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return mergeLaunchesHandler.mergeLaunches(extractProjectDetails(user, normalizeId(projectName)), user, mergeLaunchesRQ);
	}

	@Transactional
	@PostMapping(value = "/{launchId}/analyze")
	@ResponseStatus(OK)
	@ApiOperation("Start launch auto-analyzer on demand")
	public OperationCompletionRS startLaunchAnalyzer(@PathVariable String projectName, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.startLaunchAnalyzer(extractProjectDetails(user, normalizeId(projectName)), launchId);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/status")
	@ResponseStatus(OK)
	@ApiIgnore
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
			@AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) throws IOException {
		JasperPrint jasperPrint = getJasperHandler.getLaunchDetails(launchId, user);
		ReportFormat format = getJasperHandler.getReportFormat(view);
		response.setContentType(format.getContentType());
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
				String.format("attachment; filename=RP_%s_Report.%s", format.name(), format.getValue())
		);
		getJasperHandler.writeReport(format, response.getOutputStream(), jasperPrint);
	}

	@Transactional
	@DeleteMapping
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launches by ids")
	public OperationCompletionRS deleteLaunches(@PathVariable String projectName, @RequestParam(value = "ids") Long[] ids,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLaunchMessageHandler.deleteLaunches(ids, extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@PostMapping(value = "/import")
	@ResponseStatus(OK)
	@ApiOperation(value = "Import junit xml report", notes = "Only following formats are supported: zip.")
	public OperationCompletionRS importLaunch(@PathVariable String projectName, @RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal ReportPortalUser user) {
		return importLaunchHandler.importLaunch(extractProjectDetails(user, normalizeId(projectName)), user, "XUNIT", file);
	}

	private String generateLaunchLink(HttpServletRequest request, String projectName, String id) {
		return new StringBuilder(request.getScheme()).append("://")
				.append(request.getHeader("host"))
				.append("/ui/#")
				.append(projectName)
				.append("/launches/all/")
				.append(id)
				.toString();
	}

}
