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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.auth.UatClient;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.user.IDeleteUserHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delete user handler
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteUserHandler implements IDeleteUserHandler {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UatClient uatClient;

	@Override
	public OperationCompletionRS deleteUser(String userId, String principal) {
		User user = userRepository.findOne(userId);
		BusinessRule.expect(user, Predicates.notNull()).verify(ErrorType.USER_NOT_FOUND, userId);
		BusinessRule.expect(userId.equalsIgnoreCase(principal), Predicates.equalTo(false))
				.verify(ErrorType.INCORRECT_REQUEST, "You cannot delete own account");
		try {
			List<Project> userProjects = projectRepository.findUserProjects(userId);
			userProjects.forEach(project -> ProjectUtils.excludeProjectRecipients(Lists.newArrayList(user), project));
			projectRepository.removeUserFromProjects(userId);
			projectRepository.save(userProjects);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while updating projects", exp);
		}

		try {
			uatClient.revokeUserTokens(userId);
			userRepository.delete(user);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while deleting user", exp);
		}

		return new OperationCompletionRS("User with ID = '" + userId + "' successfully deleted.");
	}
}
