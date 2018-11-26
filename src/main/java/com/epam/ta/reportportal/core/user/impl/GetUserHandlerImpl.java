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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
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
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

/**
 * Implementation for GET user operations
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class GetUserHandlerImpl implements GetUserHandler {

	private final UserRepository userRepository;

	private final UserCreationBidRepository userCreationBidRepository;

	private final ProjectRepository projectRepository;

	@Autowired
	public GetUserHandlerImpl(UserRepository userRepo, UserCreationBidRepository userCreationBidRepository,
			ProjectRepository projectRepository) {
		this.userRepository = Preconditions.checkNotNull(userRepo);
		this.userCreationBidRepository = Preconditions.checkNotNull(userCreationBidRepository);
		this.projectRepository = projectRepository;
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
		User user = userRepository.findByLogin(loggedInUser.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND));
		return UserConverter.TO_RESOURCE.apply(user);
	}

	@Override
	public Iterable<UserResource> getUsers(Filter filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails) {
		// Active users only
		filter.withCondition(new FilterCondition(Condition.EQUALS, false, "false", CRITERIA_EXPIRED));
		filter.withCondition(new FilterCondition(Condition.EQUALS,
				false,
				String.valueOf(projectDetails.getProjectId()),
				CRITERIA_PROJECT_ID
		));

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
			assignedProject.setEntryType(it.getProjectType().name());
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
		Filter filter = Filter.builder()
				.withTarget(User.class)
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_USER))
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_FULL_NAME))
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_EMAIL))
				.build();
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(userRepository.findByFilter(filter, pageable));
	}
}
