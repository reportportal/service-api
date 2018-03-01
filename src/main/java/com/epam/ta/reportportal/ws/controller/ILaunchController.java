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

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Report Portal WS Interface. Launch controller
 *
 * @author Andrei Varabyeu
 * @author Andrei Kliashchonak
 * @author Andrei_Ramanchuk
 */
public interface ILaunchController {

	/**
	 * Creates new LaunchR
	 *
	 * @param project
	 * @param principal
	 * @return
	 */
	EntryCreatedRS startLaunch(String project, StartLaunchRQ startLaunchRQ, Principal principal);

	List<OperationCompletionRS> updateLaunches(String projectName, BulkRQ<UpdateLaunchRQ> rq, Principal principal);

	/**
	 * Delete LaunchR
	 *
	 * @param projectName
	 * @param launchId
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	OperationCompletionRS deleteLaunch(String launchId, String projectName, Principal principal);

	/**
	 * Finish launch
	 *
	 * @param projectName
	 * @param launchId
	 * @param finsihLaunchRQ
	 * @param principal
	 * @return
	 */
	OperationCompletionRS finishLaunch(String projectName, String launchId, FinishExecutionRQ finsihLaunchRQ, Principal principal,
			HttpServletRequest request);

	/**
	 * Stop launch by user
	 *
	 * @param projectName
	 * @param launchId
	 * @param finsihLaunchRQ
	 * @param principal
	 * @return
	 */
	OperationCompletionRS forceFinishLaunch(String projectName, String launchId, FinishExecutionRQ finsihLaunchRQ, Principal principal);

	/**
	 * Get Launch resource by specified ID
	 *
	 * @param projectName
	 * @param launchId
	 * @param principal
	 * @return
	 */
	LaunchResource getLaunch(String projectName, String launchId, Principal principal);

	/**
	 * Get all launches for specified project
	 *
	 * @param projectName
	 * @param filter
	 * @param pageble
	 * @param principal
	 * @return
	 */
	Iterable<LaunchResource> getProjectLaunches(String projectName, Filter filter, Pageable pageble, Principal principal);

	/**
	 * Get launches in specified mode for specified user or for all users
	 *
	 * @param login
	 * @param filter
	 * @param pageable
	 * @param principal
	 * @return
	 */
	Iterable<LaunchResource> getDebugLaunches(String login, Filter filter, Pageable pageable, Principal principal);

	Page<LaunchResource> getLatestLaunches(String projectName, Filter filter, Pageable pageable);

	/**
	 * Get launch tags of specified project by value (auto-complete)
	 *
	 * @param project
	 * @param value
	 * @param principal
	 * @return
	 */
	List<String> getAllTags(String project, String value, Principal principal);

	/**
	 * Get launch owners of specified project by value (auto-complete)
	 *
	 * @param project
	 * @param value
	 * @param principal
	 * @return
	 */
	List<String> getAllOwners(String project, String value, String mode, Principal principal);

	/**
	 * Get launch names of specified project by value (auto-complete)
	 *
	 * @param project
	 * @param value
	 * @param principal
	 * @return
	 */
	List<String> getAllLaunchNames(String project, String value, Principal principal);

	List<OperationCompletionRS> bulkForceFinish(String projectName, BulkRQ<FinishExecutionRQ> rq, Principal principal);

	/**
	 * Update specified by id launch
	 *
	 * @param projectName
	 * @param launchId
	 * @param updateLaunchRQ
	 * @param principal
	 * @return
	 */
	OperationCompletionRS updateLaunch(String projectName, String launchId, UpdateLaunchRQ updateLaunchRQ, Principal principal);

	/**
	 * Get comparison info of launches
	 *
	 * @param projectName
	 * @param ids
	 * @param principal
	 * @return
	 */
	Map<String, List<ChartObject>> compareLaunches(String projectName, String[] ids, Principal principal);

	/**
	 * Merge specified launches in common one
	 * Could be merged in different ways
	 *
	 * @param projectName
	 * @param mergeLaunchesRQ request data
	 * @param principal
	 * @return
	 */
	LaunchResource mergeLaunches(String projectName, MergeLaunchesRQ mergeLaunchesRQ, Principal principal);

	/**
	 * Start auto-analyzer for specified launch
	 *
	 * @param projectName Project name
	 * @param launchId    Launch id
	 * @param mode        Analyze mode
	 * @param principal   Principal
	 * @return Result message
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	OperationCompletionRS startLaunchAnalyzer(String projectName, String launchId, String mode, Principal principal)
			throws InterruptedException, ExecutionException;

	/**
	 * Get statuses of specified launches
	 *
	 * @param projectName
	 * @param ids
	 * @param principal
	 * @return
	 */
	Map<String, String> getStatuses(String projectName, String[] ids, Principal principal);

	/**
	 * Imports test results of zip archive with xml reports inside
	 *
	 * @param projectId
	 * @param file
	 * @param principal
	 * @return
	 */
	OperationCompletionRS importLaunch(String projectId, MultipartFile file, Principal principal);

	void getLaunchReport(String projectName, String launchId, String view, Principal principal, HttpServletResponse response)
			throws IOException;

	OperationCompletionRS deleteLaunches(String projectName, String[] ids, Principal principal);

}