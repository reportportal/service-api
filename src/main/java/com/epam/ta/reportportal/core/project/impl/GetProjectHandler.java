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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.project.IGetProjectHandler;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.ProjectResourceAssembler;
import com.epam.ta.reportportal.ws.converter.UserResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

/**
 * @author Andrei_Ramanchuk
 */
@Service
public class GetProjectHandler implements IGetProjectHandler {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	private UserResourceAssembler userResourceAssembler;

	@Autowired
	private ProjectResourceAssembler projectResourceAssembler;

	@Autowired
	public GetProjectHandler(ProjectRepository prjRepo) {
		this.projectRepository = Preconditions.checkNotNull(prjRepo);
	}

	@Override
	public Iterable<UserResource> getProjectUsers(String project, @FilterFor(User.class) Filter filter, Pageable pageable) {
		Project dbProject = projectRepository.findOne(project);
		if (null == dbProject || null == dbProject.getUsers()) {
			return emptyList();
		}
		String criteria = dbProject.getUsers().stream().map(Project.UserConfig::getLogin).collect(joining(","));
		filter.addCondition(new FilterCondition(Condition.IN, false, criteria, User.LOGIN));
		// Filter filter = new Filter(User.class, Condition.IN, false, criteria,
		// User.LOGIN);
		Page<User> users = userRepository.findByFilterExcluding(filter, pageable, "email");
		return userResourceAssembler.toPagedResources(users, dbProject);
	}

	@Override
	public List<String> getUserNames(String project, String value) {
		BusinessRule.expect(value.length() > 2, Predicates.equalTo(true)).verify(
				ErrorType.INCORRECT_FILTER_PARAMETERS,
				Suppliers.formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
		);
		return projectRepository.findProjectUsers(project, value);
	}

	@Override
	public com.epam.ta.reportportal.ws.model.Page<UserResource> getUserNames(String value, Pageable pageable) {
		BusinessRule.expect(value.length() >= 1, Predicates.equalTo(true)).verify(
				ErrorType.INCORRECT_FILTER_PARAMETERS,
				Suppliers.formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
		);
		return userResourceAssembler.toPagedResources(userRepository.searchForUserLogin(value, pageable));
	}

	@Override
	public ProjectResource getProject(String project) {
		Project dbProject = projectRepository.findOne(project);
		BusinessRule.expect(dbProject, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, project);

		// ======================================================
		// TODO !!!!!! UPDATE after new statistics !!!!!!
		// ======================================================

		if (null != dbProject.getConfiguration().getExternalSystem() && !dbProject.getConfiguration().getExternalSystem().isEmpty()
				&& null != dbProject.getConfiguration().getSubTypes()) {
			Iterable<ExternalSystem> systems = externalSystemRepository.findAll(dbProject.getConfiguration().getExternalSystem());
			return projectResourceAssembler.toResource(dbProject, systems);
		}

		return projectResourceAssembler.toResource(dbProject);
	}

	@Override
	public OperationCompletionRS isProjectsAvailable() {
		BusinessRule.expect(projectRepository.count() > 0, Predicates.equalTo(Boolean.TRUE))
				.verify(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR);
		return new OperationCompletionRS("MongoDB project collection available");
	}

	@Override
	public List<String> getAllProjectNames() {
		return projectRepository.findAllProjectNames();
	}
}
