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

package com.epam.ta.reportportal.core.launch.impl;

import static com.epam.reportportal.model.ValidationConstraints.MAX_LAUNCH_NAME_LENGTH;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.INCORRECT_FILTER_PARAMETERS;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Preconditions.HAS_ANY_MODE;
import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.querygen.Condition.EQUALS;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_AUTOMATION_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_NO_DEFECT_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_PRODUCT_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_SYSTEM_ISSUE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_TO_INVESTIGATE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_FAILED;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_PASSED;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_SKIPPED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.event.GetLaunchResourceCollectionEvent;
import com.epam.reportportal.model.launch.cluster.ClusterInfoResource;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.constants.LaunchReportConstants;
import com.epam.ta.reportportal.core.jasper.util.JasperDataProvider;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.cluster.GetClusterHandler;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.reporting.LaunchResource;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.GetLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLaunchHandlerImpl implements GetLaunchHandler {

  private final GetClusterHandler getClusterHandler;
  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;
  private final ItemAttributeRepository itemAttributeRepository;
  private final ProjectRepository projectRepository;
  private final WidgetContentRepository widgetContentRepository;
  private final UserRepository userRepository;
  private final JasperDataProvider dataProvider;
  private final GetJasperReportHandler<Launch> jasperReportHandler;
  private final LaunchConverter launchConverter;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public GetLaunchHandlerImpl(GetClusterHandler getClusterHandler,
      LaunchRepository launchRepository, TestItemRepository testItemRepository,
      ItemAttributeRepository itemAttributeRepository, ProjectRepository projectRepository,
      WidgetContentRepository widgetContentRepository, UserRepository userRepository,
      JasperDataProvider dataProvider,
      @Qualifier("launchJasperReportHandler") GetJasperReportHandler<Launch> jasperReportHandler,
      LaunchConverter launchConverter, ApplicationEventPublisher applicationEventPublisher) {
    this.getClusterHandler = getClusterHandler;
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.itemAttributeRepository = itemAttributeRepository;
    this.projectRepository = projectRepository;
    this.widgetContentRepository = widgetContentRepository;
    this.userRepository = userRepository;
    this.dataProvider = Preconditions.checkNotNull(dataProvider);
    this.jasperReportHandler = jasperReportHandler;
    this.launchConverter = launchConverter;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public Launch get(Long id) {
    return launchRepository.findById(id)
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, id));
  }

  @Override
  public LaunchResource getLaunch(String launchId, MembershipDetails membershipDetails) {
    final Launch launch = findLaunch(launchId, membershipDetails);
    return getLaunchResource(launch);
  }

  private Launch findLaunch(String launchId, MembershipDetails membershipDetails) {
    Launch launch;
    try {
      launch = get(Long.parseLong(launchId));
    } catch (NumberFormatException e) {
      launch = launchRepository.findByUuid(launchId)
          .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
    }
    validate(launch, membershipDetails);
    return launch;
  }

  @Override
  public LaunchResource getLaunchByProjectKey(String projectKey, Pageable pageable, Filter filter,
      String username) {
    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    Page<Launch> launches =
        launchRepository.findByFilter(ProjectFilter.of(filter, project.getId()), pageable);
    expect(launches, notNull()).verify(LAUNCH_NOT_FOUND);
    return getLaunchResource(launches.iterator().next());
  }

  private LaunchResource getLaunchResource(Launch launch) {
    final LaunchResource launchResource = launchConverter.TO_RESOURCE.apply(launch);
    applicationEventPublisher.publishEvent(
        new GetLaunchResourceCollectionEvent(Collections.singletonList(launchResource)));
    return launchResource;
  }

  @Override
  public com.epam.ta.reportportal.model.Page<LaunchResource> getProjectLaunches(
      MembershipDetails membershipDetails,
      Filter filter, Pageable pageable, String userName) {
    validateModeConditions(filter);
    Project project = projectRepository.findById(membershipDetails.getProjectId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND,
            membershipDetails.getProjectId()
        ));

    filter = addLaunchCommonCriteria(Mode.DEFAULT, filter);
    Page<Launch> launches =
        launchRepository.findByFilter(ProjectFilter.of(filter, project.getId()), pageable);
    return getLaunchResources(launches);
  }

  /*
   * Changed logic for this method: It should return DEBUG launches for
   * project users, for specified user or only owner
   */
  @Override
  public com.epam.ta.reportportal.model.Page<LaunchResource> getDebugLaunches(
      MembershipDetails membershipDetails,
      Filter filter, Pageable pageable) {
    validateModeConditions(filter);
    filter = addLaunchCommonCriteria(Mode.DEBUG, filter);
    Page<Launch> launches =
        launchRepository.findByFilter(ProjectFilter.of(filter, membershipDetails.getProjectId()),
            pageable
        );
    return getLaunchResources(launches);
  }

  @Override
  public List<String> getAttributeKeys(MembershipDetails membershipDetails,
      String value) {
    return itemAttributeRepository.findLaunchAttributeKeys(membershipDetails.getProjectId(), value,
        false
    );
  }

  @Override
  public List<String> getAttributeValues(MembershipDetails membershipDetails, String key,
      String value) {
    return itemAttributeRepository.findLaunchAttributeValues(membershipDetails.getProjectId(), key,
        value, false
    );
  }

  @Override
  public com.epam.ta.reportportal.model.Page<LaunchResource> getLatestLaunches(
      MembershipDetails membershipDetails,
      Filter filter, Pageable pageable) {

    validateModeConditions(filter);

    Project project = projectRepository.findById(membershipDetails.getProjectId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND,
            membershipDetails.getProjectId()
        ));

    filter = addLaunchCommonCriteria(Mode.DEFAULT, filter);

    Page<Launch> launches =
        launchRepository.findAllLatestByFilter(ProjectFilter.of(filter, project.getId()), pageable);
    return getLaunchResources(launches);
  }

  @Override
  @Transactional(readOnly = true)
  public com.epam.ta.reportportal.model.Page<ClusterInfoResource> getClusters(String launchId,
      MembershipDetails membershipDetails, Pageable pageable) {
    final Launch launch = findLaunch(launchId, membershipDetails);
    return getClusterHandler.getResources(launch, pageable);
  }

  @Override
  public boolean hasItemsWithIssues(Launch launch) {
    return testItemRepository.hasItemsWithIssueByLaunch(launch.getId());
  }

  private com.epam.ta.reportportal.model.Page<LaunchResource> getLaunchResources(
      Page<Launch> launches) {
    final com.epam.ta.reportportal.model.Page<LaunchResource> launchResourcePage =
        PagedResourcesAssembler.pageConverter(launchConverter.TO_RESOURCE).apply(launches);
    applicationEventPublisher.publishEvent(
        new GetLaunchResourceCollectionEvent(launchResourcePage.getContent()));
    return launchResourcePage;
  }

  @Override
  public List<String> getLaunchNames(MembershipDetails membershipDetails, String value) {
    expect(value.length() <= MAX_LAUNCH_NAME_LENGTH, equalTo(true)).verify(
        INCORRECT_FILTER_PARAMETERS,
        formattedSupplier("Length of the launch name string '{}' more than {} symbols", value,
            MAX_LAUNCH_NAME_LENGTH
        )
    );
    return launchRepository.getLaunchNamesByModeExcludedByStatus(membershipDetails.getProjectId(),
        value, LaunchModeEnum.DEFAULT, StatusEnum.IN_PROGRESS
    );
  }

  @Override
  public List<String> getOwners(MembershipDetails membershipDetails, String value,
      String mode) {
    expect(value.length() > 2, equalTo(true)).verify(INCORRECT_FILTER_PARAMETERS,
        formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
    );

    LaunchModeEnum launchMode = LaunchModeEnum.findByName(mode).orElseThrow(
        () -> new ReportPortalException(ErrorType.INCORRECT_FILTER_PARAMETERS,
            formattedSupplier("Mode - {} doesn't exist.", mode)
        ));

    return launchRepository.getOwnerNames(membershipDetails.getProjectId(), value, launchMode.name());
  }

  @Override
  public Map<String, List<ChartStatisticsContent>> getLaunchesComparisonInfo(
      MembershipDetails membershipDetails, Long[] ids) {

    List<String> contentFields =
        Lists.newArrayList(DEFECTS_AUTOMATION_BUG_TOTAL, DEFECTS_NO_DEFECT_TOTAL,
            DEFECTS_PRODUCT_BUG_TOTAL, DEFECTS_SYSTEM_ISSUE_TOTAL, DEFECTS_TO_INVESTIGATE_TOTAL,
            EXECUTIONS_FAILED, EXECUTIONS_PASSED, EXECUTIONS_SKIPPED
        );

    Filter filter = Filter.builder().withTarget(Launch.class).withCondition(
        new FilterCondition(Condition.IN, false,
            Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(",")), CRITERIA_ID
        )).withCondition(
        new FilterCondition(EQUALS, false, String.valueOf(membershipDetails.getProjectId()),
            CRITERIA_PROJECT_ID
        )).build();

    List<ChartStatisticsContent> result =
        widgetContentRepository.launchesComparisonStatistics(filter, contentFields, Sort.unsorted(),
            ids.length
        );

    return singletonMap(RESULT, result);

  }

  @Override
  public Map<String, String> getStatuses(MembershipDetails membershipDetails,
      Long[] ids) {
    return launchRepository.getStatuses(membershipDetails.getProjectId(), ids);
  }

  @Override
  public void exportLaunch(Long launchId, ReportFormat reportFormat, OutputStream outputStream,
      ReportPortalUser user, MembershipDetails membershipDetails) {

    Launch launch = launchRepository.findById(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
    expect(launch.getStatus(), not(statusIn(IN_PROGRESS))).verify(ErrorType.FORBIDDEN_OPERATION,
        Suppliers.formattedSupplier(
            "Launch '{}' has IN_PROGRESS status. Impossible to export such elements.", launchId)
    );

    validate(launch, membershipDetails);

    String userFullName = userRepository.findById(user.getUserId()).map(User::getFullName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUserId()));

    Map<String, Object> params = jasperReportHandler.convertParams(launch);

    fillWithAdditionalParams(params, launch, userFullName);

    JasperPrint jasperPrint = jasperReportHandler.getJasperPrint(params, new JREmptyDataSource());

    jasperReportHandler.writeReport(reportFormat, outputStream, jasperPrint);

  }

  /**
   * Validate launch affiliation to the project
   *
   * @param launch         {@link Launch}
   * @param membershipDetails {@link MembershipDetails}
   */
  private void validate(Launch launch, MembershipDetails membershipDetails) {
    expect(launch.getProjectId(), Predicates.equalTo(membershipDetails.getProjectId()))
        .verify(ACCESS_DENIED);
  }

  /**
   * Add to filter project and mode criteria
   *
   * @param filter Filter to update
   * @return Updated filter
   */
  private Filter addLaunchCommonCriteria(Mode mode, Filter filter) {
    return ofNullable(filter).orElseGet(() -> new Filter(Launch.class, Lists.newArrayList()))
        .withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_MODE, mode.name()).build());
  }

  /**
   * Validate if filter doesn't contain any "mode" related conditions.
   *
   * @param filter
   */
  private void validateModeConditions(Filter filter) {
    expect(filter.getFilterConditions().stream().map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream).anyMatch(HAS_ANY_MODE), equalTo(false)).verify(
        INCORRECT_FILTER_PARAMETERS,
        "Filters for 'mode' aren't applicable for project's launches."
    );
  }

  private void fillWithAdditionalParams(Map<String, Object> params, Launch launch,
      String userFullName) {

    Optional<String> owner = userRepository.findById(launch.getUserId()).map(User::getFullName);

    /* Check if launch owner still in system if not - setup principal */
    params.put(LaunchReportConstants.OWNER, owner.orElse(userFullName));

    params.put(LaunchReportConstants.TEST_ITEMS, dataProvider.getTestItemsOfLaunch(launch));
  }

}
