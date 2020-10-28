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
import com.epam.ta.reportportal.commons.EntityUtils;
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
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.ws.model.user.CreateUserRS;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;

import static com.epam.ta.reportportal.auth.UserRoleHierarchy.ROLE_REGISTERED;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.entity.project.ProjectRole.forName;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class SaveDefaultProjectService {

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final PersonalProjectService personalProjectService;

	private final MailServiceFactory emailServiceFactory;

	private final ShareableObjectsHandler aclHandler;

	private final ThreadPoolTaskExecutor emailExecutorService;

	private final PasswordEncoder passwordEncoder;

	@Autowired
	public SaveDefaultProjectService(ProjectRepository projectRepository, UserRepository userRepository,
			PersonalProjectService personalProjectService, MailServiceFactory emailServiceFactory, ShareableObjectsHandler aclHandler,
			ThreadPoolTaskExecutor emailExecutorService, PasswordEncoder passwordEncoder) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.personalProjectService = personalProjectService;
		this.emailServiceFactory = emailServiceFactory;
		this.aclHandler = aclHandler;
		this.emailExecutorService = emailExecutorService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public Pair<UserActivityResource, CreateUserRS> saveDefaultProject(CreateUserRQFull request, String basicUrl) {
		String projectName = EntityUtils.normalizeId(request.getDefaultProject());
		Project defaultProject = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		UserRole userRole = UserRole.findByName(request.getAccountRole())
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter."));

		ProjectRole projectRole = forName(request.getProjectRole()).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND,
				request.getProjectRole()
		));

		User user = new UserBuilder().addCreateUserFullRQ(request)
				.addUserRole(userRole)
				.addPassword(passwordEncoder.encode(request.getPassword()))
				.get();

		ProjectUser assignedProjectUser = new ProjectUser().withProjectRole(projectRole).withUser(user).withProject(defaultProject);
		user.getProjects().add(assignedProjectUser);

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
			ofNullable(basicUrl).ifPresent(url -> emailExecutorService.execute(() -> emailServiceFactory.getDefaultEmailService(true)
					.sendCreateUserConfirmationEmail(request, url)));
		} catch (PersistenceException pe) {
			if (pe.getCause() instanceof ConstraintViolationException) {
				fail().withError(RESOURCE_ALREADY_EXISTS, ((ConstraintViolationException) pe.getCause()).getConstraintName());
			}
			throw new ReportPortalException("Error while User creating: " + pe.getMessage(), pe);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User creating: " + exp.getMessage(), exp);
		}

		authenticateUser(user);

		if (projectRole.sameOrHigherThan(ProjectRole.PROJECT_MANAGER)) {
			aclHandler.permitSharedObjects(defaultProject.getId(), user.getLogin(), BasePermission.ADMINISTRATION);
		} else {
			aclHandler.permitSharedObjects(defaultProject.getId(), user.getLogin(), BasePermission.READ);
		}

		response.setId(user.getId());
		response.setLogin(user.getLogin());
		return Pair.of(TO_ACTIVITY_RESOURCE.apply(user, defaultProject.getId()), response);
	}

	/**
	 * Required for {@link org.springframework.security.acls.domain.AclAuthorizationStrategy#securityCheck(Acl, int)} with custom implementation
	 * {@link com.epam.ta.reportportal.auth.acl.ReportPortalAclAuthorizationStrategyImpl} to permit shared objects to the newly created user
	 *
	 * @param user {@link User}
	 */
	private void authenticateUser(User user) {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user.getLogin(),
						user.getPassword(),
						Sets.newHashSet(new SimpleGrantedAuthority(ROLE_REGISTERED))
				));
	}
}
