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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.converter.converters.UserConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.*;
import static com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils.getAnalyzerConfig;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetProjectHandlerImpl implements GetProjectHandler {

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final GetJasperReportHandler<Project> jasperReportHandler;

	private final IntegrationRepository integrationRepository;

	private final ProjectConverter projectConverter;

	@Autowired
	public GetProjectHandlerImpl(ProjectRepository projectRepository, UserRepository userRepository,
			@Qualifier("projectJasperReportHandler") GetJasperReportHandler<Project> jasperReportHandler,
			IntegrationRepository integrationRepository, ProjectConverter projectConverter) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.jasperReportHandler = jasperReportHandler;
		this.integrationRepository = integrationRepository;
		this.projectConverter = projectConverter;
	}

	@Override
	public Iterable<UserResource> getProjectUsers(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		if (CollectionUtils.isEmpty(project.getUsers())) {
			return Collections.emptyList();
		}
		filter.withCondition(new FilterCondition(Condition.EQUALS,
				false,
				String.valueOf(projectDetails.getProjectId()),
				CRITERIA_PROJECT_ID
		));
		Page<User> users = userRepository.findByFilterExcluding(filter, pageable, "email");
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(users);
	}

	@Override
	public ProjectResource getProject(String projectName, ReportPortalUser user) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		Optional<ProjectUser> projectUser = project.getUsers()
				.stream()
				.filter(it -> it.getId().getUserId().equals(user.getUserId()))
				.findAny();

		BusinessRule.expect(projectUser, Optional::isPresent)
				.verify(ErrorType.PROJECT_DOESNT_CONTAIN_USER, projectName, user.getUsername());

		List<Long> integrationTypeIds = project.getIntegrations()
				.stream()
				.map(Integration::getType)
				.map(IntegrationType::getId)
				.collect(Collectors.toList());

		List<Integration> globalIntegrations = integrationRepository.findAllGlobalNotInIntegrationTypeIds(integrationTypeIds);

		project.getIntegrations().addAll(globalIntegrations);

		return projectConverter.TO_PROJECT_RESOURCE.apply(project);
	}

	@Override
	public List<String> getUserNames(ReportPortalUser.ProjectDetails projectDetails, String value) {
		BusinessRule.expect(value.length() > 2, Predicates.equalTo(true)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				Suppliers.formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
		);
		return userRepository.findNamesByProject(projectDetails.getProjectId(), value);
	}

	@Override
	public Iterable<UserResource> getUserNames(String value, Pageable pageable) {
		BusinessRule.expect(value.length() >= 1, Predicates.equalTo(true)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				Suppliers.formattedSupplier("Length of the filtering string '{}' is less than 1 symbol", value)
		);
		Filter filter = Filter.builder()
				.withTarget(User.class)
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, value, CRITERIA_USER))
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, value, CRITERIA_FULL_NAME))
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, value, CRITERIA_EMAIL))
				.build();
		return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(userRepository.findByFilter(filter, pageable));
	}

	@Override
	public List<String> getAllProjectNames() {
		return projectRepository.findAllProjectNames();
	}

	@Override
	public List<String> getAllProjectNamesByTerm(String term) {
		return projectRepository.findAllProjectNamesByTerm(term);
	}

	@Override
	public void exportProjects(ReportFormat reportFormat, Filter filter, OutputStream outputStream) {

		List<Project> projects = projectRepository.findByFilter(filter);

		List<? extends Map<String, ?>> data = projects.stream().map(jasperReportHandler::convertParams).collect(Collectors.toList());

		JRDataSource jrDataSource = new JRBeanCollectionDataSource(data);

		//don't provide any params to not overwrite params from the Jasper template
		JasperPrint jasperPrint = jasperReportHandler.getJasperPrint(null, jrDataSource);

		jasperReportHandler.writeReport(reportFormat, outputStream, jasperPrint);
	}

	@Override
	public Map<String, Boolean> getAnalyzerIndexingStatus() {
		return projectRepository.findAll()
				.stream()
				.collect(Collectors.toMap(Project::getName, it -> getAnalyzerConfig(it).isIndexingRunning()));
	}

}
