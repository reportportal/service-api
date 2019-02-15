/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.PersonalProjectService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.builders.UserBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.ws.model.user.CreateUserRS;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static com.epam.reportportal.commons.Safe.safe;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.forName;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class SaveDefaultProjectService {

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final PersonalProjectService personalProjectService;

	private final MailServiceFactory emailServiceFactory;

	@Autowired
	public SaveDefaultProjectService(ProjectRepository projectRepository, UserRepository userRepository,
			PersonalProjectService personalProjectService, MailServiceFactory emailServiceFactory) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.personalProjectService = personalProjectService;
		this.emailServiceFactory = emailServiceFactory;
	}

	@Transactional
	public Pair<UserActivityResource, CreateUserRS> saveDefaultProject(CreateUserRQFull request, String email, String basicUrl) {
		String projectName = EntityUtils.normalizeId(request.getDefaultProject());
		Project defaultProject = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		CreateUserRQConfirm req = new CreateUserRQConfirm();
		req.setDefaultProject(projectName);
		req.setEmail(email);
		req.setFullName(request.getFullName());
		req.setLogin(request.getLogin());
		req.setPassword(request.getPassword());

		final Optional<UserRole> userRole = UserRole.findByName(request.getAccountRole());
		expect(userRole, Preconditions.IS_PRESENT).verify(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter.");
		//noinspection ConstantConditions
		User user = new UserBuilder().addCreateUserRQ(req).addUserRole(userRole.get()).get();
		Optional<ProjectRole> projectRole = forName(request.getProjectRole());
		expect(projectRole, Preconditions.IS_PRESENT).verify(ROLE_NOT_FOUND, request.getProjectRole());

		Set<ProjectUser> projectUsers = defaultProject.getUsers();
		//noinspection ConstantConditions
		ProjectUser assignedProjectUser = new ProjectUser().withProjectRole(projectRole.get()).withUser(user).withProject(defaultProject);
		projectUsers.add(assignedProjectUser);
		defaultProject.setUsers(projectUsers);

		CreateUserRS response = new CreateUserRS();

		try {
			/*
			 * Generate and save personal project for the user
			 */
			Project personalProject = projectRepository.save(personalProjectService.generatePersonalProject(user));

			user.getProjects().add(assignedProjectUser);
			user.getProjects()
					.add(personalProject.getUsers()
							.stream()
							.findFirst()
							.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, personalProject.getName())));
			userRepository.save(user);

			safe(
					() -> emailServiceFactory.getDefaultEmailService(true).sendCreateUserConfirmationEmail(request, basicUrl),
					e -> response.setWarning(e.getMessage())
			);
		} catch (DuplicateKeyException e) {
			fail().withError(USER_ALREADY_EXISTS, formattedSupplier("email='{}'", request.getEmail()));
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User creating: " + exp.getMessage(), exp);
		}

		response.setLogin(user.getLogin());

		return Pair.of(TO_ACTIVITY_RESOURCE.apply(user, defaultProject.getId()), response);
	}
}
