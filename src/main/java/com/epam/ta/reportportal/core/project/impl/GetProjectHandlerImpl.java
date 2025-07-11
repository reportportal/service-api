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

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT;
import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_KEY;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EMAIL;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_FULL_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilterCondition;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.project.ProjectResource;
import com.epam.ta.reportportal.model.user.SearchUserResource;
import com.epam.ta.reportportal.model.user.UserResource;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.converter.converters.UserConverter;
import com.epam.reportportal.rules.exception.ErrorType;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetProjectHandlerImpl implements GetProjectHandler {

  private static final String LENGTH_LESS_THAN_1_SYMBOL_MSG =
      "Length of the filtering string " + "'{}' is less than 1 symbol";

  private final ProjectRepository projectRepository;

  private final UserRepository userRepository;

  private final GetJasperReportHandler<ProjectInfo> jasperReportHandler;

  private final ProjectConverter projectConverter;


  @Autowired
  public GetProjectHandlerImpl(ProjectRepository projectRepository, UserRepository userRepository,
      @Qualifier("projectJasperReportHandler")
      GetJasperReportHandler<ProjectInfo> jasperReportHandler, ProjectConverter projectConverter) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.jasperReportHandler = jasperReportHandler;
    this.projectConverter = projectConverter;
  }

  @Override
  public com.epam.ta.reportportal.model.Page<UserResource> getProjectUsers(MembershipDetails membershipDetails, Filter filter,
                                                                           Pageable pageable, ReportPortalUser user) {
    Project project = projectRepository.findByKey(membershipDetails.getProjectKey())
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND,
            membershipDetails.getProjectKey()));
    if (CollectionUtils.isEmpty(project.getUsers())) {
      return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(Page.empty(pageable));
    }

    // exclude email field from the response for non-administrator and non-manager users
    var isAdminOrManager = user.getUserRole().equals(UserRole.ADMINISTRATOR)
        || membershipDetails.getOrgRole().equals(OrganizationRole.MANAGER);
    var excludeFieldsArray = isAdminOrManager ? new String[0] : new String[]{"email"};

    filter.withCondition(
        new FilterCondition(Condition.EQUALS, false, project.getKey(), CRITERIA_PROJECT_KEY));
    Page<User> users = userRepository.findProjectUsersByFilterExcluding(project.getKey(), filter,
        pageable, excludeFieldsArray);

    return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(users);
  }

  @Override
  public boolean exists(Long id) {
    return projectRepository.existsById(id);
  }

  @Override
  public Project get(MembershipDetails membershipDetails) {
    return projectRepository.findById(membershipDetails.getProjectId()).orElseThrow(
        () -> new ReportPortalException(PROJECT_NOT_FOUND, membershipDetails.getProjectName()));
  }

  @Override
  public Project get(Long id) {
    return projectRepository.findById(id)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, id));
  }

  @Override
  public Project get(String name) {
    return projectRepository.findByKey(name)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, name));
  }

  @Override
  public Project getRaw(String name) {
    return projectRepository.findRawByName(name)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, name));
  }

  @Override
  public ProjectResource getResource(String projectKey, ReportPortalUser user) {

    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    return projectConverter.TO_PROJECT_RESOURCE.apply(project);
  }

  @Override
  public List<String> getUserNames(MembershipDetails membershipDetails, String value) {
    checkBusinessRuleLessThan1Symbol(value);
    return userRepository.findNamesByProject(membershipDetails.getProjectId(), value);
  }

  private void checkBusinessRuleLessThan1Symbol(String value) {
    BusinessRule.expect(value.length() >= 1, Predicates.equalTo(true))
        .verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
            Suppliers.formattedSupplier(LENGTH_LESS_THAN_1_SYMBOL_MSG, value)
        );
  }

  @Override
  public com.epam.ta.reportportal.model.Page<SearchUserResource> getUserNames(String value,
                                                                              MembershipDetails membershipDetails, UserRole userRole, Pageable pageable) {
    checkBusinessRuleLessThan1Symbol(value);

    final CompositeFilterCondition userCondition = (userRole.equals(UserRole.ADMINISTRATOR))
        ? getUserSearchSuggestCondition(value) : getUserSearchCondition(value);

    final Filter filter = Filter.builder().withTarget(User.class).withCondition(userCondition)
        .withCondition(
            new FilterCondition(Operator.AND, Condition.ANY, true, membershipDetails.getProjectName(),
                CRITERIA_PROJECT
            )).build();

    return PagedResourcesAssembler.pageConverter(UserConverter.TO_SEARCH_RESOURCE)
        .apply(userRepository.findByFilterExcludingProjects(filter, pageable));
  }

	private CompositeFilterCondition getUserSearchSuggestCondition(String value) {
		return new CompositeFilterCondition(List.of(new FilterCondition(Operator.OR, Condition.CONTAINS, false, value, CRITERIA_USER),
				new FilterCondition(Operator.OR, Condition.CONTAINS, false, value, CRITERIA_FULL_NAME),
				new FilterCondition(Operator.OR, Condition.CONTAINS, false, value, CRITERIA_EMAIL)
		), Operator.AND);
	}

	private CompositeFilterCondition getUserSearchCondition(String value) {
		return new CompositeFilterCondition(List.of(
				new FilterCondition(Operator.OR, Condition.EQUALS, false, value, CRITERIA_EMAIL)
		), Operator.AND);
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
  public void exportProjects(ReportFormat reportFormat, Queryable filter,
      OutputStream outputStream) {

    List<ProjectInfo> projects = projectRepository.findProjectInfoByFilter(filter);

    List<? extends Map<String, ?>> data =
        projects.stream().map(jasperReportHandler::convertParams).collect(Collectors.toList());

    JRDataSource jrDataSource = new JRBeanCollectionDataSource(data);

    //don't provide any params to not overwrite params from the Jasper template
    JasperPrint jasperPrint = jasperReportHandler.getJasperPrint(null, jrDataSource);

    jasperReportHandler.writeReport(reportFormat, outputStream, jasperPrint);
  }

  @Override
  public Map<String, Boolean> getAnalyzerIndexingStatus() {
    return projectRepository.findAll().stream().collect(
        Collectors.toMap(Project::getName, it -> getAnalyzerConfig(it).isIndexingRunning()));
  }

}
