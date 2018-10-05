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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.UatClient;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.user.DeleteUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Delete user handler
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteUserHandlerImpl implements DeleteUserHandler {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UatClient uatClient;

//	@Autowired
//	private ILogIndexer logIndexer;

	@Override
	public OperationCompletionRS deleteUser(String login, ReportPortalUser loggedInUser) {
		User user = userRepository.findByLogin(login).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, login));
		BusinessRule.expect(login.equalsIgnoreCase(loggedInUser.getUsername()), Predicates.equalTo(false))
				.verify(ErrorType.INCORRECT_REQUEST, "You cannot delete own account");
		try {
			List<Project> userProjects = projectRepository.findUserProjects(login);
			userProjects.forEach(project -> ProjectUtils.excludeProjectRecipients(Lists.newArrayList(user), project));
			projectRepository.saveAll(userProjects);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while updating projects", exp);
		}

		Optional<String> personalProjectName = projectRepository.findPersonalProjectName(user.getLogin());

		try {
			uatClient.revokeUserTokens(login);
			userRepository.delete(user);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while deleting user", exp);
		}

		//TODO analyzer
//		personalProjectName.ifPresent(s -> logIndexer.deleteIndex(s));

		return new OperationCompletionRS("User with ID = '" + login + "' successfully deleted.");
	}
}
