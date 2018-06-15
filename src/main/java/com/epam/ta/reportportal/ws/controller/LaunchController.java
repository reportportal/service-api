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
import com.epam.ta.reportportal.core.launch.*;
import com.epam.ta.reportportal.store.commons.querygen.Filter;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Controller implementation for
 * {@link com.epam.ta.reportportal.store.database.entity.launch.Launch} entity
 * <p>
 * Note: please use EntityUtils for forced lower case for user names and project
 * names
 * </p>
 *
 * @author Andrei Varabyeu
 * @author Andrei Kliashchonak
 * @author Andrei_Ramanchuk
 */
@Controller
@RequestMapping("/{projectName}/launch")
//@PreAuthorize(ASSIGNED_TO_PROJECT)
public class LaunchController {

	@Autowired
	private IStartLaunchHandler createLaunchMessageHandler;

	@Autowired
	private IFinishLaunchHandler finishLaunchMessageHandler;

	@Autowired
	private IDeleteLaunchHandler deleteLaunchMessageHandler;

	@Autowired
	private IGetLaunchHandler getLaunchMessageHandler;

	@Autowired
	private IUpdateLaunchHandler updateLaunchHandler;

	//	@Autowired
	//	private IGetJasperReportHandler getJasperHandler;

	@Autowired
	private IMergeLaunchHandler mergeLaunchesHandler;

	//	@Autowired
	//	private ImportLaunchHandler importLaunchHandler;

	@PostMapping
	@Transactional
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Starts launch for specified project")
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startLaunch(
			@ApiParam(value = "Name of project launch starts under", required = true) @PathVariable String projectName,
			@ApiParam(value = "Start launch request body", required = true) @RequestBody @Validated StartLaunchRQ startLaunchRQ,
			@AuthenticationPrincipal ReportPortalUser reportPortalUser) {
		ProjectUtils.extractProjectDetails(reportPortalUser, projectName);
		return createLaunchMessageHandler.startLaunch(reportPortalUser, projectName, startLaunchRQ);
	}

	@RequestMapping(value = "/{launchId}/finish", method = PUT)
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	//@PreAuthorize(ALLOWED_TO_REPORT)
	@ApiOperation("Finish launch for specified project")
	public OperationCompletionRS finishLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated FinishExecutionRQ finishLaunchRQ, @AuthenticationPrincipal ReportPortalUser user,
			HttpServletRequest request) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return finishLaunchMessageHandler.finishLaunch(launchId, finishLaunchRQ, projectName, user);
	}

	@PutMapping("/{launchId}/stop")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch for specified project")
	public OperationCompletionRS forceFinishLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated FinishExecutionRQ finishExecutionRQ, @AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return finishLaunchMessageHandler.stopLaunch(launchId, finishExecutionRQ, projectName, user);
	}

	@PutMapping("/stop")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch")
	public List<OperationCompletionRS> bulkForceFinish(@PathVariable String projectName,
			@RequestBody @Validated BulkRQ<FinishExecutionRQ> rq, @AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return finishLaunchMessageHandler.stopLaunch(rq, projectName, user);
	}

	@PutMapping("/{launchId}/update")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Updates launch for specified project")
	public OperationCompletionRS updateLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@RequestBody @Validated UpdateLaunchRQ updateLaunchRQ, @AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return updateLaunchHandler.updateLaunch(launchId, projectName, user, updateLaunchRQ);
	}

	@PutMapping("/update")
	@Transactional
	@ResponseBody
	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ResponseStatus(OK)
	@ApiOperation("Updates launches for specified project")
	public List<OperationCompletionRS> updateLaunches(@PathVariable String projectName, @RequestBody @Validated BulkRQ<UpdateLaunchRQ> rq,
			@AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return updateLaunchHandler.updateLaunch(rq, projectName, user);
	}

	@DeleteMapping("/{launchId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launch by ID")
	public OperationCompletionRS deleteLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return deleteLaunchMessageHandler.deleteLaunch(launchId, projectName, user);
	}

	@GetMapping("/{launchId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get specified launch")
	public LaunchResource getLaunch(@PathVariable String projectName, @PathVariable Long launchId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getLaunchMessageHandler.getLaunch(launchId, ProjectUtils.extractProjectDetails(user, projectName));
	}

	@GetMapping
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get list of project launches by filter")
	public Iterable<LaunchResource> getProjectLaunches(@PathVariable String projectName, @FilterFor(Launch.class) Filter filter,
			@SortFor(Launch.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return getLaunchMessageHandler.getProjectLaunches(projectName, filter, pageable, user.getUsername());
	}

	@RequestMapping(value = "/latest", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get list of latest project launches by filter")
	public Page<LaunchResource> getLatestLaunches(@PathVariable String projectName, @FilterFor(Launch.class) Filter filter,
			@SortFor(Launch.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return getLaunchMessageHandler.getLatestLaunches(projectName, filter, pageable);
	}

	@RequestMapping(value = "/tags", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get all unique tags of project launches")
	public List<String> getAllTags(@PathVariable String projectName, @RequestParam(value = "filter." + "cnt." + "tags") String value,
			@AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return getLaunchMessageHandler.getTags(projectName, value);
	}

	@RequestMapping(value = "/owners", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get all unique owners of project launches")
	public List<String> getAllOwners(@PathVariable String projectName, @RequestParam(value = "filter." + "cnt." + "user") String value,
			@RequestParam(value = "mode", required = false, defaultValue = "DEFAULT") String mode,
			@AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return getLaunchMessageHandler.getOwners(projectName, value, mode);
	}

	@RequestMapping(value = "/names", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get launch names of project")
	public List<String> getAllLaunchNames(@PathVariable String projectName, @RequestParam(value = "filter." + "cnt." + "name") String value,
			@AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return getLaunchMessageHandler.getLaunchNames(projectName, value);
	}

	//	@RequestMapping(value = "/compare", method = GET)
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation("Compare launches")
	//	public Map<String, List<ChartObject>> compareLaunches(@PathVariable String projectName, @RequestParam(value = "ids") Long[] ids,
	//			@AuthenticationPrincipal ReportPortalUser user) {
	//		return getLaunchMessageHandler.getLaunchesComparisonInfo(normalizeId(projectName), ids);
	//	}

	//
	//	@PostMapping("/merge")
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation("Merge set of specified launches in common one")
	//	public LaunchResource mergeLaunches(
	//			@ApiParam(value = "Name of project contains merging launches under", required = true) @PathVariable String projectName,
	//			@ApiParam(value = "Merge launches request body", required = true) @RequestBody @Validated MergeLaunchesRQ mergeLaunchesRQ,
	//			@AuthenticationPrincipal ReportPortalUser user) {
	//		return mergeLaunchesHandler.mergeLaunches(normalizeId(projectName), user, mergeLaunchesRQ);
	//	}
	//
	//	@RequestMapping(value = "/{launchId}/analyze", method = POST)
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation("Start launch auto-analyzer on demand")
	//	public OperationCompletionRS startLaunchAnalyzer(@PathVariable String projectName, @PathVariable Long launchId, @AuthenticationPrincipal ReportPortalUser user) {
	//		return updateLaunchHandler.startLaunchAnalyzer(normalizeId(projectName), launchId);
	//	}
	//
	@RequestMapping(value = "/status", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiIgnore
	public Map<String, String> getStatuses(@PathVariable String projectName, @RequestParam(value = "ids") Long[] ids,
			@AuthenticationPrincipal ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		return getLaunchMessageHandler.getStatuses(projectName, ids);
	}
	//
	//	@RequestMapping(value = "/{launchId}/report", method = RequestMethod.GET)
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation(value = "Export specified launch", notes = "Only following formats are supported: pdf (by default), xls, html.")
	//	public void getLaunchReport(@PathVariable String projectName, @PathVariable Long launchId,
	//			@ApiParam(allowableValues = "pdf, xls, html") @RequestParam(value = "view", required = false, defaultValue = "pdf") String view,
	//			@AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) throws IOException {
	//
	//		throw new UnsupportedOperationException();
	//		//		JasperPrint jasperPrint = getJasperHandler.getLaunchDetails(launchId, user);
	//		//
	//		//		ReportFormat format = getJasperHandler.getReportFormat(view);
	//		//		response.setContentType(format.getContentType());
	//		//		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
	//		//				String.format("attachment; filename=RP_%s_Report.%s", format.name(), format.getValue())
	//		//		);
	//		//
	//		//		getJasperHandler.writeReport(format, response.getOutputStream(), jasperPrint);
	//
	//	}

	@DeleteMapping
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Delete specified launches by ids")
	public OperationCompletionRS deleteLaunches(@PathVariable String projectName, @RequestParam(value = "ids") Long[] ids,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteLaunchMessageHandler.deleteLaunches(ids, projectName, user);
	}
	//
	//	@RequestMapping(value = "/import", method = RequestMethod.POST)
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation(value = "Import junit xml report", notes = "Only following formats are supported: zip.")
	//	public OperationCompletionRS importLaunch(@PathVariable String projectName, @RequestParam("file") MultipartFile file,
	//			@AuthenticationPrincipal ReportPortalUser user) {
	//		throw new UnsupportedOperationException();
	//		//return importLaunchHandler.importLaunch(normalizeId(projectName), user, "XUNIT", file);
	//	}
}
