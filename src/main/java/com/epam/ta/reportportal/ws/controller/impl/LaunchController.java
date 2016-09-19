/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.jasper.IGetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.ReportFormat;
import com.epam.ta.reportportal.core.launch.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.controller.ILaunchController;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.net.HttpHeaders;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MEMBER;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Controller implementation for {@link com.epam.ta.reportportal.database.entity.Launch} entity
 * <p>
 * Note: please use EntityUtils for forced lower case for user names and project names
 * </p>
 *
 * @author Andrei Varabyeu
 * @author Andrei Kliashchonak
 * @author Andrei_Ramanchuk
 */
@Controller
@RequestMapping("/{projectName}/launch")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class LaunchController implements ILaunchController {

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

	@Autowired
	private IGetJasperReportHandler getJasperHandler;

	@Override
	@RequestMapping(method = POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Starts launch for specified project")
	public EntryCreatedRS startLaunch(
			@ApiParam(value = "Name of project launch starts under", required = true) @PathVariable String projectName,
			@ApiParam(value = "Start launch request body", required = true) @RequestBody @Validated StartLaunchRQ startLaunchRQ,
			Principal principal) {
		return createLaunchMessageHandler.startLaunch(principal.getName(), EntityUtils.normalizeProjectName(projectName), startLaunchRQ);
	}

	@Override
	@RequestMapping(value = "/{launchId}/finish", method = PUT)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Finish launch for specified project")
	public OperationCompletionRS finishLaunch(@PathVariable String projectName, @PathVariable String launchId,
			@RequestBody @Validated FinishExecutionRQ finishLaunchRQ, Principal principal, HttpServletRequest request) {
		return finishLaunchMessageHandler.finishLaunch(launchId, finishLaunchRQ, EntityUtils.normalizeProjectName(projectName),
				principal.getName());
	}

	@Override
	@RequestMapping(value = "/{launchId}/stop", method = PUT)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Force finish launch for specified project")
	public OperationCompletionRS forceFinishLaunch(@PathVariable String projectName, @PathVariable String launchId,
			@RequestBody @Validated FinishExecutionRQ finsihLaunchRQ, Principal principal) {
		return finishLaunchMessageHandler.stopLaunch(launchId, finsihLaunchRQ, EntityUtils.normalizeProjectName(projectName),
				principal.getName());
	}

	@Override
	@RequestMapping(value = "/{launchId}/update", method = PUT)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	// TODO temporary solution. Spring handles only last @PreAuthorize
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Updates launch for specified project")
	public OperationCompletionRS updateLaunch(@PathVariable String projectName, @PathVariable String launchId,
			@RequestBody @Validated UpdateLaunchRQ updateLaunchRQ, Principal principal) {
		return updateLaunchHandler.updateLaunch(launchId, EntityUtils.normalizeProjectName(projectName), principal.getName(),
				updateLaunchRQ);
	}

	@Override
	@RequestMapping(value = "/{launchId}", method = DELETE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete specified launch by ID")
	public OperationCompletionRS deleteLaunch(@PathVariable String projectName, @PathVariable String launchId, Principal principal) {
		return deleteLaunchMessageHandler.deleteLaunch(launchId, EntityUtils.normalizeProjectName(projectName), principal.getName());
	}

	@Override
	@RequestMapping(value = "/{launchId}", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get specified launch")
	public LaunchResource getLaunch(@PathVariable String projectName, @PathVariable String launchId, Principal principal) {
		return getLaunchMessageHandler.getLaunch(launchId, principal.getName(), EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of project launches by filter")
	public Iterable<LaunchResource> getProjectLaunches(@PathVariable String projectName, @FilterFor(Launch.class) Filter filter,
			@SortFor(Launch.class) Pageable pageable, Principal principal) {
		return getLaunchMessageHandler.getProjectLaunches(EntityUtils.normalizeProjectName(projectName), filter, pageable,
				principal.getName());
	}

	@Override
	@RequestMapping(value = "/mode", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(PROJECT_MEMBER)
	@ApiOperation("Get launches of specified project from DEBUG mode")
	public Iterable<LaunchResource> getDebugLaunches(@PathVariable String projectName, @FilterFor(Launch.class) Filter filter,
			@SortFor(Launch.class) Pageable pageble, Principal principal) {
		return getLaunchMessageHandler.getDebugLaunches(EntityUtils.normalizeProjectName(projectName), principal.getName(), filter,
				pageble);
	}

	@Override
	@RequestMapping(value = "/tags", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all unique tags of project launches")
	public List<String> getAllTags(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + Launch.TAGS, required = true) String value,
			Principal principal) {
		return getLaunchMessageHandler.getTags(EntityUtils.normalizeProjectName(projectName), value);
	}

	@Override
	@RequestMapping(value = "/owners", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all unique owners of project launches")
	public List<String> getAllOwners(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + Launch.USER, required = true) String value,
			@RequestParam(value = "mode", required = false, defaultValue = "DEFAULT") String mode, Principal principal) {
		return getLaunchMessageHandler.getOwners(EntityUtils.normalizeProjectName(projectName), value, "userRef", mode);
	}

	@Override
	@RequestMapping(value = "/names", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get launch names of project")
	public List<String> getAllLaunchNames(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + Launch.NAME, required = true) String value,
			Principal principal) {
		return getLaunchMessageHandler.getLaunchNames(EntityUtils.normalizeProjectName(projectName), value);
	}

	@Override
	@RequestMapping(value = "/compare", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Compare launches")
	public Map<String, List<ChartObject>> compareLaunches(@PathVariable String projectName,
			@RequestParam(value = "ids", required = true) String[] ids, Principal principal) {
		return getLaunchMessageHandler.getLaunchesComparisonInfo(EntityUtils.normalizeProjectName(projectName), ids);
	}

	@Override
	@RequestMapping(value = "/merge", method = POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	// Owner and project LEAD+ permissions validated inside cause no information
	// on top resource level
	@ApiOperation("Merge set of specified launches in common one")
	public LaunchResource mergeLaunches(
			@ApiParam(value = "Name of project contains merging launches under", required = true) @PathVariable String projectName,
			@ApiParam(value = "Merge launches request body", required = true) @RequestBody @Validated MergeLaunchesRQ mergeLaunchesRQ,
			Principal principal) {
		return updateLaunchHandler.mergeLaunches(EntityUtils.normalizeProjectName(projectName), principal.getName(), mergeLaunchesRQ);
	}

	@Override
	@RequestMapping(value = "/{launchId}/analyze/{strategy}", method = POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Start launch auto-analyzer on demand")
	public OperationCompletionRS startLaunchAnalyzer(@PathVariable String projectName, @PathVariable String launchId,
			@PathVariable String strategy, Principal principal) throws InterruptedException, ExecutionException {
		return updateLaunchHandler.startLaunchAnalyzer(EntityUtils.normalizeProjectName(projectName), launchId, strategy);
	}

	@Override
	@RequestMapping(value = "/status", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Map<String, String> getStatuses(@PathVariable String projectName, @RequestParam(value = "ids") String[] ids,
			Principal principal) {
		return getLaunchMessageHandler.getStatuses(EntityUtils.normalizeProjectName(projectName), ids);
	}

	@Override
	@RequestMapping(value = "/{launchId}/report", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Export specified launch", notes = "Only following formats are supported: pdf (by default), xml, xls, html.")
	public void getLaunchReport(@PathVariable String projectName, @PathVariable String launchId,
			@RequestParam(value = "view", required = false, defaultValue = "pdf") String view, Principal principal,
			HttpServletResponse response) throws IOException {

		JasperPrint jasperPrint = getJasperHandler.getLaunchDetails(launchId, principal.getName());

		ReportFormat format = getJasperHandler.getReportFormat(view);
		response.setContentType(format.getContentType());
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
				String.format("attachment; filename=RP_%s_Report.%s", format.name(), format.getValue()));

		getJasperHandler.writeReport(format, response.getOutputStream(), jasperPrint);

	}
}