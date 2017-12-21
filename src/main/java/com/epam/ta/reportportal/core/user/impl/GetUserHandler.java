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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.user.IGetUserHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserCreationBid;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.UserResourceAssembler;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.UserBidRS;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.USER_NOT_FOUND;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * Implementation for GET user operations
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class GetUserHandler implements IGetUserHandler {

	private UserRepository userRepository;
	private UserResourceAssembler userResourceAssembler;
	private UserCreationBidRepository userCreationBidRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	public GetUserHandler(UserRepository userRepo, UserCreationBidRepository userBidRepo, UserResourceAssembler userResourceAsm) {
		this.userRepository = Preconditions.checkNotNull(userRepo);
		this.userCreationBidRepository = Preconditions.checkNotNull(userBidRepo);
		this.userResourceAssembler = Preconditions.checkNotNull(userResourceAsm);
	}

	@Override
	public UserResource getUser(String username, Principal principal) {
		User user = userRepository.findOne(username.toLowerCase());
		expect(user, notNull()).verify(USER_NOT_FOUND, username);
		return userResourceAssembler.toResource(user);
	}

	@Override
	public Iterable<UserResource> getUsers(Filter filter, Pageable pageable, String projectName) {
		// Active users only
		filter.addCondition(new FilterCondition(Condition.EQUALS, false, "false", User.EXPIRED));
		Project project = projectRepository.findOne(projectName);
		String criteria = project.getUsers().stream().map(Project.UserConfig::getLogin).collect(joining(","));
		filter.addCondition(new FilterCondition(Condition.IN, true, criteria, User.LOGIN));
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, project);
		return userResourceAssembler.toPagedResources(userRepository.findByFilterExcluding(filter, pageable, "email"));
	}

	@Override
	public UserBidRS getBidInformation(String uuid) {
		UserCreationBid bid = userCreationBidRepository.findOne(uuid);
		UserBidRS response = new UserBidRS();
		if (null != bid) {
			response.setIsActive(true);
			response.setEmail(bid.getEmail());
			response.setId(bid.getId());
		} else {
			response.setIsActive(false);
		}
		return response;
	}

	@Override
	public YesNoRS validateInfo(String username, String email) {
		if (null != username) {
			User user = userRepository.findOne(EntityUtils.normalizeId(username));
			return null != user ? new YesNoRS(true) : new YesNoRS(false);
		} else if (null != email) {
			User user = userRepository.findByEmail(EntityUtils.normalizeId(email));
			return null != user ? new YesNoRS(true) : new YesNoRS(false);
		}
		return new YesNoRS(false);
	}

	@Override
	public Map<String, UserResource.AssignedProject> getUserProjects(String userName) {
		return projectRepository.findUserProjects(userName).stream().collect(toMap(Project::getName, it -> {
			UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
			assignedProject.setEntryType(it.getConfiguration().getEntryType().name());
			Project.UserConfig userConfig = ProjectUtils.findUserConfigByLogin(it, userName);

			ofNullable(userConfig.getProjectRole()).ifPresent(role -> assignedProject.setProjectRole(role.name()));
			ofNullable(userConfig.getProposedRole()).ifPresent(role -> assignedProject.setProposedRole(role.name()));

			return assignedProject;
		}));
	}

	@Override
	public Iterable<UserResource> getAllUsers(Filter filter, Pageable pageable) {
		final Page<User> users = userRepository.findByFilter(filter, pageable);
		return userResourceAssembler.toPagedResources(users);
	}

	@Override
	public Iterable<UserResource> searchUsers(String term, Pageable pageable) {
		return userResourceAssembler.toPagedResources(userRepository.searchForUser(term, pageable));
	}
}
