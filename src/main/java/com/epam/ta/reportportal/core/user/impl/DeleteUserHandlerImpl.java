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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.project.DeleteProjectHandler;
import com.epam.ta.reportportal.core.user.DeleteUserHandler;
import com.epam.ta.reportportal.core.user.content.remover.UserContentRemover;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.*;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Delete user handler
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Service
@Transactional
public class DeleteUserHandlerImpl implements DeleteUserHandler {

	private final UserBinaryDataService dataStore;

	private final UserRepository userRepository;

	private final DeleteProjectHandler deleteProjectHandler;

	private final ShareableObjectsHandler shareableObjectsHandler;

	private final UserContentRemover userContentRemover;

	private final ProjectRepository projectRepository;

	@Autowired
	public DeleteUserHandlerImpl(UserRepository userRepository, DeleteProjectHandler deleteProjectHandler,
			ShareableObjectsHandler shareableObjectsHandler, UserBinaryDataService dataStore, UserContentRemover userContentRemover,
			ProjectRepository projectRepository) {
		this.userRepository = userRepository;
		this.deleteProjectHandler = deleteProjectHandler;
		this.shareableObjectsHandler = shareableObjectsHandler;
		this.dataStore = dataStore;
		this.userContentRemover = userContentRemover;
		this.projectRepository = projectRepository;
	}

	@Override
	public OperationCompletionRS deleteUser(Long userId, ReportPortalUser loggedInUser) {
		User user = userRepository.findById(userId).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));
		BusinessRule.expect(Objects.equals(userId, loggedInUser.getUserId()), Predicates.equalTo(false))
				.verify(ErrorType.INCORRECT_REQUEST, "You cannot delete own account");

		userContentRemover.removeContent(user);

		List<Project> userProjects = projectRepository.findUserProjects(user.getLogin());
		userProjects.forEach(project -> {
			if (ProjectUtils.isPersonalForUser(project.getProjectType(), project.getName(), user.getLogin())) {
				deleteProjectHandler.deleteProject(project.getId());
			} else {
				shareableObjectsHandler.preventSharedObjects(project.getId(), user.getLogin());
				ProjectUtils.excludeProjectRecipients(Lists.newArrayList(user), project);
			}
		});

		dataStore.deleteUserPhoto(user);
		userRepository.delete(user);
		return new OperationCompletionRS("User with ID = '" + userId + "' successfully deleted.");
	}

	@Override
	public DeleteBulkRS deleteUsers(DeleteBulkRQ deleteBulkRQ, ReportPortalUser currentUser) {
		List<ReportPortalException> exceptions = Lists.newArrayList();
		List<Long> deleted = Lists.newArrayList();
		deleteBulkRQ.getIds().forEach(userId -> {
			try {
				deleteUser(userId, currentUser);
				deleted.add(userId);
			} catch (ReportPortalException rp) {
				exceptions.add(rp);
			}
		});
		return new DeleteBulkRS(deleted, Collections.emptyList(), exceptions.stream().map(ex -> {
			ErrorRS errorResponse = new ErrorRS();
			errorResponse.setErrorType(ex.getErrorType());
			errorResponse.setMessage(ex.getMessage());
			return errorResponse;
		}).collect(Collectors.toList()));
	}
}
