/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.project.impl;

import static com.epam.reportportal.base.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_KEY;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.jasper.GetJasperReportHandler;
import com.epam.reportportal.base.core.jasper.ReportFormat;
import com.epam.reportportal.base.core.project.GetProjectHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.Predicates;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.project.ProjectResource;
import com.epam.reportportal.base.model.user.UserResource;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import com.epam.reportportal.base.ws.converter.converters.ProjectConverter;
import com.epam.reportportal.base.ws.converter.converters.UserConverter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Fetches project resources and user assignments.
 *
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
  public com.epam.reportportal.base.model.Page<UserResource> getProjectUsers(MembershipDetails membershipDetails,
      Filter filter,
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
        () -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, membershipDetails.getProjectName()));
  }

  @Override
  public Project get(Long id) {
    return projectRepository.findById(id)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Project " + id));
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
  public List<String> getAllProjectNames() {
    return projectRepository.findAllProjectNames();
  }

  @Override
  public List<String> getAllProjectNamesByTerm(String term) {
    return projectRepository.findAllProjectNamesByTerm(term);
  }

  @Override
  public void exportProjects(ReportFormat reportFormat, Queryable filter,
      HttpServletResponse outputStream) {
    var projects = projectRepository.findProjectInfoByFilter(filter);
    var data = projects.stream().map(jasperReportHandler::convertParams).collect(Collectors.toList());
    var jrDataSource = new JRBeanCollectionDataSource(data);

    //don't provide any params to not overwrite params from the Jasper template
    var jasperPrint = jasperReportHandler.getJasperPrint(null, jrDataSource);

    var bytes = jasperReportHandler.exportReportBytes(reportFormat, jasperPrint);
    try (var output = outputStream.getOutputStream()) {
      output.write(bytes);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unable to write data to the response."
      );
    }
  }

  @Override
  public Map<String, Boolean> getAnalyzerIndexingStatus() {
    return projectRepository.findAll().stream().collect(
        Collectors.toMap(Project::getName, it -> getAnalyzerConfig(it).isIndexingRunning()));
  }

}
