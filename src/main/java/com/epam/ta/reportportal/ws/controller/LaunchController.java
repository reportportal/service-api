/*
 * Copyright 2017 EPAM Systems
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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
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
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.net.HttpHeaders;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
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

	@PostMapping
	@Transactional
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Starts launch for specified project")
	public EntryCreatedRS startLaunch(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@ApiParam(value = "Start launch request body", required = true) @RequestBody @Validated StartLaunchRQ startLaunchRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createLaunchMessageHandler.startLaunch(user, projectDetails, startLaunchRQ);
	}

	@PutMapping(value = "/{launchId}/finish")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Finish launch for specified project")
	public OperationCompletionRS finishLaunch(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long launchId,
			@RequestBody @Validated FinishExecutionRQ finishLaunchRQ, @AuthenticationPrincipal ReportPortalUser user,
			HttpServletRequest request) {
		return finishLaunchMessageHandler.finishLaunch(launchId, finishLaunchRQ, projectDetails, user);
	}

	@PutMapping("/{launchId}/stop")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch for specified project")
	public OperationCompletionRS forceFinishLaunch(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@PathVariable Long launchId, @RequestBody @Validated FinishExecutionRQ finishExecutionRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return finishLaunchMessageHandler.stopLaunch(launchId, finishExecutionRQ, projectDetails, user);
	}

	@PutMapping("/stop")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch")
	public List<OperationCompletionRS> bulkForceFinish(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated BulkRQ<FinishExecutionRQ> rq, @AuthenticationPrincipal ReportPortalUser user) {
		return finishLaunchMessageHandler.stopLaunch(rq, projectDetails, user);
	}

	@PutMapping("/{launchId}/update")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Updates launch for specified project")
	public OperationCompletionRS updateLaunch(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long launchId,
			@RequestBody @Validated UpdateLaunchRQ updateLaunchRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.updateLaunch(launchId, projectDetails, user, updateLaunchRQ);
	}

	@PutMapping("/update")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Updates launches for specified project")
	public List<OperationCompletionRS> updateLaunches(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated BulkRQ<UpdateLaunchRQ> rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.updateLaunch(rq, projectDetails, user);
	}

	@DeleteMapping("/{launchId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launch by ID")
	public OperationCompletionRS deleteLaunch(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLaunchMessageHandler.deleteLaunch(launchId, projectDetails, user);
	}

	@GetMapping("/{launchId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get specified launch")
	public LaunchResource getLaunch(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunch(launchId, projectDetails, user.getUsername());
	}

	@GetMapping
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get list of project launches by filter")
	public Iterable<LaunchResource> getProjectLaunches(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@FilterFor(Launch.class) Filter filter, @SortFor(Launch.class) Pageable pageable,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getProjectLaunches(projectDetails, filter, pageable, user.getUsername());
	}

	@GetMapping(value = "/latest")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get list of latest project launches by filter")
	public Page<LaunchResource> getLatestLaunches(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@FilterFor(Launch.class) Filter filter, @SortFor(Launch.class) Pageable pageable,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLatestLaunches(projectDetails, filter, pageable);
	}

	@GetMapping(value = "/tags")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get all unique tags of project launches")
	public List<String> getAllTags(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "filter." + "cnt." + "tags") String value, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getTags(projectDetails, value);
	}

	@GetMapping(value = "/owners")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get all unique owners of project launches")
	public List<String> getAllOwners(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "filter." + "cnt." + "user") String value,
			@RequestParam(value = "mode", required = false, defaultValue = "DEFAULT") String mode,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getOwners(projectDetails, value, mode);
	}

	@GetMapping(value = "/names")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get launch names of project")
	public List<String> getAllLaunchNames(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "filter." + "cnt." + "name") String value, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunchNames(projectDetails, value);
	}

	@GetMapping(value = "/compare")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Compare launches")
	public Map<String, List<LaunchesStatisticsContent>> compareLaunches(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunchesComparisonInfo(projectDetails, ids);
	}

	@PostMapping("/merge")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Merge set of specified launches in common one")
	public LaunchResource mergeLaunches(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@ApiParam(value = "Merge launches request body", required = true) @RequestBody @Validated MergeLaunchesRQ mergeLaunchesRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return mergeLaunchesHandler.mergeLaunches(projectDetails, user, mergeLaunchesRQ);
	}

	@PostMapping(value = "/{launchId}/analyze")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Start launch auto-analyzer on demand")
	public OperationCompletionRS startLaunchAnalyzer(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@PathVariable Long launchId, @AuthenticationPrincipal ReportPortalUser user) {
		return updateLaunchHandler.startLaunchAnalyzer(projectDetails, launchId);
	}

	@GetMapping(value = "/status")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiIgnore
	public Map<String, String> getStatuses(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getStatuses(projectDetails, ids);
	}

	@GetMapping(value = "/{launchId}/report")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation(value = "Export specified launch", notes = "Only following formats are supported: pdf (by default), xls, html.")
	public void getLaunchReport(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long launchId,
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

	@DeleteMapping
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launches by ids")
	public OperationCompletionRS deleteLaunches(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteLaunchMessageHandler.deleteLaunches(ids, projectDetails, user);
	}

	@PostMapping(value = "/import")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation(value = "Import junit xml report", notes = "Only following formats are supported: zip.")
	public OperationCompletionRS importLaunch(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam("file") MultipartFile file, @AuthenticationPrincipal ReportPortalUser user) {
		return importLaunchHandler.importLaunch(projectDetails, user, "XUNIT", file);
	}

	@ModelAttribute
	private ReportPortalUser.ProjectDetails projectDetails(@PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return ProjectUtils.extractProjectDetails(user, normalizeId(projectName));
	}
}
