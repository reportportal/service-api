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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.PersonalProjectService;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.UserConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.UserBidRS;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.google.common.base.Preconditions;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

	private final PersonalProjectService personalProjectService;

	private final GetJasperReportHandler<User> jasperReportHandler;

	@Autowired
	public GetUserHandlerImpl(UserRepository userRepo, UserCreationBidRepository userCreationBidRepository,
			ProjectRepository projectRepository, PersonalProjectService personalProjectService,
			@Qualifier("userJasperReportHandler") GetJasperReportHandler<User> jasperReportHandler) {
		this.userRepository = Preconditions.checkNotNull(userRepo);
		this.userCreationBidRepository = Preconditions.checkNotNull(userCreationBidRepository);
		this.projectRepository = projectRepository;
		this.personalProjectService = personalProjectService;
		this.jasperReportHandler = jasperReportHandler;
	}

	@Override
	public UserResource getUser(String username, ReportPortalUser loggedInUser) {

		User user = userRepository.findByLogin(username.toLowerCase())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
		return UserConverter.TO_RESOURCE.apply(user);
	}

	@Override
	public UserResource getUser(ReportPortalUser loggedInUser) {
		User user = userRepository.findByLogin(loggedInUser.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
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
			return user.isPresent() ? new YesNoRS(true) : new YesNoRS(false);
		} else if (null != email) {
			Optional<User> user = userRepository.findByEmail(EntityUtils.normalizeId(email));
			return user.isPresent() ? new YesNoRS(true) : new YesNoRS(false);
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
	public Iterable<UserResource> getAllUsers(Queryable filter, Pageable pageable) {
		final Page<User> users = userRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(users);
	}

	@Override
	public void exportUsers(ReportFormat reportFormat, OutputStream outputStream, Queryable filter) {

		final List<User> users = userRepository.findByFilter(filter);

		List<? extends Map<String, ?>> data = users.stream().map(jasperReportHandler::convertParams).collect(Collectors.toList());

		JRDataSource jrDataSource = new JRBeanCollectionDataSource(data);

		//don't provide any params to not overwrite params from the Jasper template
		JasperPrint jasperPrint = jasperReportHandler.getJasperPrint(null, jrDataSource);

		jasperReportHandler.writeReport(reportFormat, outputStream, jasperPrint);
	}

	@Override
	public Iterable<UserResource> searchUsers(String term, Pageable pageable) {

		Filter filter = Filter.builder()
				.withTarget(User.class)
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_USER))
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_EMAIL))
				.build();
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(userRepository.findByFilter(filter, pageable));

	}

}
