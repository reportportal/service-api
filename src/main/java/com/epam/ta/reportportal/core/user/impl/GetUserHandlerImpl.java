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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.UserConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.UserBidRS;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.EXPIRED;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.LOGIN;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * Implementation for GET user operations
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class GetUserHandlerImpl implements GetUserHandler {

	private UserRepository userRepository;
	private UserCreationBidRepository userCreationBidRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	public GetUserHandlerImpl(UserRepository userRepo, UserCreationBidRepository userCreationBidRepository) {
		this.userRepository = Preconditions.checkNotNull(userRepo);
		this.userCreationBidRepository = Preconditions.checkNotNull(userCreationBidRepository);
	}

	@Override
	public UserResource getUser(String username, ReportPortalUser loggedInUser) {

		//todo validate permissions
		User user = userRepository.findByLogin(username.toLowerCase())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND));
		return UserConverter.TO_RESOURCE.apply(user);
	}

	@Override
	public UserResource getUser(ReportPortalUser loggedInUser) {
		//todo check for lower case if necessary
		User user = userRepository.findByLogin(loggedInUser.getUsername()/*.toLowerCase()*/)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND));
		return UserConverter.TO_RESOURCE.apply(user);
	}

	@Override
	public Iterable<UserResource> getUsers(Filter filter, Pageable pageable, String projectName) {
		// Active users only
		filter.withCondition(new FilterCondition(Condition.EQUALS, false, "false", EXPIRED));
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND));
		String criteria = project.getUsers().stream().map(ProjectUser::getUser).map(User::getLogin).collect(joining(","));
		filter.withCondition(new FilterCondition(Condition.IN, true, criteria, LOGIN));
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE)
				.apply(userRepository.findByFilterExcluding(filter, pageable, "email"));
	}

	@Override
	public UserBidRS getBidInformation(String uuid) {
		Optional<UserCreationBid> bid = userCreationBidRepository.findById(uuid);
		return bid.map(b -> {
			UserBidRS rs = new UserBidRS();
			rs.setIsActive(true);
			rs.setEmail(b.getEmail());
			rs.setUuid(b.getUuid());
			return rs;
		}).orElseGet(() -> {
			UserBidRS rs = new UserBidRS();
			rs.setIsActive(false);
			return rs;
		});
	}

	@Override
	public YesNoRS validateInfo(String username, String email) {
		if (null != username) {
			Optional<User> user = userRepository.findByLogin(EntityUtils.normalizeId(username));
			return null != user ? new YesNoRS(true) : new YesNoRS(false);
		} else if (null != email) {
			Optional<User> user = userRepository.findByEmail(EntityUtils.normalizeId(email));
			return null != user ? new YesNoRS(true) : new YesNoRS(false);
		}
		return new YesNoRS(false);
	}

	@Override
	public Map<String, UserResource.AssignedProject> getUserProjects(String userName) {
		return projectRepository.findUserProjects(userName).stream().collect(toMap(Project::getName, it -> {
			UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
			assignedProject.setEntryType(it.getConfiguration().get(ProjectAttributeEnum.ENTRY_TYPE.getAttribute()));
			ProjectUser projectUser = ProjectUtils.findUserConfigByLogin(it, userName);

			ofNullable(ofNullable(projectUser).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userName))
					.getProjectRole()).ifPresent(role -> assignedProject.setProjectRole(role.name()));

			return assignedProject;
		}));
	}

	@Override
	public Iterable<UserResource> getAllUsers(Filter filter, Pageable pageable) {
		final Page<User> users = userRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(users);
	}

	@Override
	public Iterable<UserResource> searchUsers(String term, Pageable pageable) {
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(userRepository.searchForUser(term, pageable));
	}
}
