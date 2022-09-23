/*
 * Copyright 2022 EPAM Systems
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
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.core.project.settings.notification.CreateProjectNotificationHandler;
import com.epam.ta.reportportal.core.project.settings.notification.DeleteProjectNotificationHandlerImpl;
import com.epam.ta.reportportal.core.project.settings.notification.GetProjectNotificationsHandler;
import com.epam.ta.reportportal.core.project.settings.notification.UpdateProjectNotificationHandler;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT_OR_ADMIN;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@RestController
@RequestMapping("/v1/notification")
public class NotificationController {

	private final GetProjectHandler getProjectHandler;
	private final GetProjectNotificationsHandler getProjectNotificationsHandler;
	private final CreateProjectNotificationHandler createProjectNotificationHandler;
	private final UpdateProjectNotificationHandler updateProjectNotificationHandler;
	private final DeleteProjectNotificationHandlerImpl deleteNotificationHandler;

	@Autowired
	public NotificationController(GetProjectHandler getProjectHandler, GetProjectNotificationsHandler getProjectNotificationsHandler,
			CreateProjectNotificationHandler createProjectNotificationHandler,
			UpdateProjectNotificationHandler updateProjectNotificationHandler, DeleteProjectNotificationHandlerImpl deleteNotificationHandler) {
		this.getProjectHandler = getProjectHandler;
		this.getProjectNotificationsHandler = getProjectNotificationsHandler;
		this.createProjectNotificationHandler = createProjectNotificationHandler;
		this.updateProjectNotificationHandler = updateProjectNotificationHandler;
		this.deleteNotificationHandler = deleteNotificationHandler;
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectKey}")
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Returns notifications config of specified project", notes = "Only for users assigned to specified project")
	public List<SenderCaseDTO> getNotifications(@PathVariable String projectKey) {
		return getProjectNotificationsHandler.getProjectNotifications(getProjectHandler.get(normalizeId(projectKey)).getId());
	}

	@Transactional
	@PostMapping("/{projectKey}")
	@ResponseStatus(CREATED)
	@PreAuthorize(ASSIGNED_TO_PROJECT_OR_ADMIN)
	@ApiOperation(value = "Creates notification for specified project", notes = "Only for users with PROJECT_MANAGER or ADMIN roles")
	public EntryCreatedRS createNotification(@PathVariable String projectKey,
			@RequestBody @Validated SenderCaseDTO createNotificationRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createProjectNotificationHandler.createNotification(
				getProjectHandler.get(normalizeId(projectKey)),
				createNotificationRQ,
				user
		);
	}

	@Transactional
	@PutMapping("/{projectKey}")
	@ResponseStatus(CREATED)
	@PreAuthorize(ASSIGNED_TO_PROJECT_OR_ADMIN)
	@ApiOperation(value = "Updates notification for specified project", notes = "Only for users with PROJECT_MANAGER or ADMIN roles")
	public OperationCompletionRS updateNotification(@PathVariable String projectKey,
			@RequestBody @Validated SenderCaseDTO updateNotificationRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectNotificationHandler.updateNotification(
				getProjectHandler.get(normalizeId(projectKey)),
				updateNotificationRQ,
				user
		);
	}

	@Transactional
	@DeleteMapping("/{projectKey}/{notificationId:\\d+}")
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT_OR_ADMIN)
	@ApiOperation(value = "Deletes notification for specified project", notes = "Only for users with PROJECT_MANAGER or ADMIN roles")
	public OperationCompletionRS deleteNotification(@PathVariable String projectKey,
			@PathVariable Long notificationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteNotificationHandler.deleteNotification(
				getProjectHandler.get(normalizeId(projectKey)),
				notificationId,
				user
		);
	}

}
