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

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.Condition.EQUALS;
import static com.epam.ta.reportportal.commons.querygen.Condition.GREATER_THAN_OR_EQUALS;
import static com.epam.ta.reportportal.commons.querygen.Condition.IN;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATED_AT;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_EVENT_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_CREATION_DATE;
import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_NAME;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.DELETE_DEFECT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.LINK_ISSUE;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.LINK_ISSUE_AA;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UNLINK_ISSUE;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_DEFECT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_ITEM;
import static com.epam.ta.reportportal.ws.converter.converters.ActivityConverter.TO_RESOURCE;
import static com.epam.ta.reportportal.ws.converter.converters.ActivityConverter.TO_RESOURCE_WITH_USER;
import static com.epam.ta.reportportal.ws.reporting.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.reporting.ErrorType.PROJECT_NOT_FOUND;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.project.GetProjectInfoHandler;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.project.email.ProjectInfoWidget;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.model.project.LaunchesPerUser;
import com.epam.ta.reportportal.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.converter.converters.ProjectSettingsConverter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.google.common.collect.Lists;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetProjectInfoHandlerImpl implements GetProjectInfoHandler {

  private static final Double WEEKS_IN_MONTH = 4.4;
  private static final int LIMIT = 150;
  private static final Predicate<ActivityAction> ACTIVITIES_PROJECT_FILTER =
      it -> it == UPDATE_DEFECT || it == DELETE_DEFECT || it == LINK_ISSUE || it == LINK_ISSUE_AA
          || it == UNLINK_ISSUE || it == UPDATE_ITEM;
  private final ProjectRepository projectRepository;

  private final LaunchRepository launchRepository;

  private final ActivityRepository activityRepository;

  private final ProjectInfoWidgetDataConverter dataConverter;

  private final LaunchConverter launchConverter;

  private final UserRepository userRepository;

  private final TicketRepository ticketRepository;

  private final DecimalFormat formatter = new DecimalFormat("###.##");

  @Autowired
  public GetProjectInfoHandlerImpl(ProjectRepository projectRepository,
      LaunchRepository launchRepository, ActivityRepository activityRepository,
      ProjectInfoWidgetDataConverter dataConverter, LaunchConverter launchConverter,
      UserRepository userRepository, TicketRepository ticketRepository) {
    this.projectRepository = projectRepository;
    this.launchRepository = launchRepository;
    this.activityRepository = activityRepository;
    this.dataConverter = dataConverter;
    this.launchConverter = launchConverter;
    this.userRepository = userRepository;
    this.ticketRepository = ticketRepository;
  }

  /**
   * Utility method for calculation of start interval date
   *
   * @param interval Back interval
   * @return Now minus interval
   */
  private static LocalDateTime getStartIntervalDate(InfoInterval interval) {
    return LocalDateTime.now(Clock.systemUTC()).minusMonths(interval.getCount());
  }

  /**
   * Filter that gets project info from selected date.
   *
   * @param project      Project
   * @param infoInterval Date interval
   * @return {@link Filter}
   */
  private static Filter projectInfoFilter(Project project, InfoInterval infoInterval) {
    return Filter.builder().withTarget(ProjectInfo.class)
        .withCondition(new FilterCondition(EQUALS, false, project.getName(), CRITERIA_PROJECT_NAME))
        .withCondition(new FilterCondition(GREATER_THAN_OR_EQUALS, false, String.valueOf(
            getStartIntervalDate(infoInterval).toInstant(ZoneOffset.UTC).toEpochMilli()),
            CRITERIA_PROJECT_CREATION_DATE
        )).build();
  }

  @Override
  public Iterable<ProjectInfoResource> getAllProjectsInfo(Queryable filter, Pageable pageable) {
    return PagedResourcesAssembler.pageConverter(ProjectSettingsConverter.TO_PROJECT_INFO_RESOURCE)
        .apply(projectRepository.findProjectInfoByFilter(filter, pageable));
  }

  @Override
  public ProjectInfoResource getProjectInfo(String projectKey, String interval) {

    Project project = projectRepository.findByKey(normalizeId(projectKey))
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectKey));

    InfoInterval infoInterval = InfoInterval.findByInterval(interval)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, interval));

    Filter filter = Filter.builder().withTarget(ProjectInfo.class).withCondition(
        FilterCondition.builder().eq(CRITERIA_PROJECT_NAME, project.getName()).build()).build();

    Page<ProjectInfo> result =
        projectRepository.findProjectInfoByFilter(filter, Pageable.unpaged());
    ProjectInfoResource projectInfoResource =
        ProjectSettingsConverter.TO_PROJECT_INFO_RESOURCE.apply(result.get().findFirst()
            .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectKey)));

    LocalDateTime startIntervalDate = getStartIntervalDate(infoInterval);

    Map<String, Integer> countPerUser =
        launchRepository.countLaunchesGroupedByOwner(project.getId(),
            LaunchModeEnum.DEFAULT.toString(), startIntervalDate
        );

    projectInfoResource.setUniqueTickets(
        ticketRepository.findUniqueCountByProjectBefore(project.getId(), startIntervalDate));

    projectInfoResource.setLaunchesPerUser(
        countPerUser.entrySet().stream().map(e -> new LaunchesPerUser(e.getKey(), e.getValue()))
            .collect(Collectors.toList()));

    if (projectInfoResource.getLaunchesQuantity() != 0) {
      formatter.setRoundingMode(RoundingMode.HALF_UP);
      double value =
          projectInfoResource.getLaunchesQuantity() / (infoInterval.getCount() * WEEKS_IN_MONTH);
      projectInfoResource.setLaunchesPerWeek(formatter.format(value));
    } else {
      projectInfoResource.setLaunchesPerWeek(formatter.format(0));
    }
    return projectInfoResource;
  }

  @Override
  public Map<String, ?> getProjectInfoWidgetContent(String projectKey, String interval,
      String widgetCode) {
    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectKey));

    InfoInterval infoInterval = InfoInterval.findByInterval(interval)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, interval));

    ProjectInfoWidget widgetType = ProjectInfoWidget.findByCode(widgetCode)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, widgetCode));

    List<Launch> launches =
        launchRepository.findByProjectIdAndStartTimeGreaterThanAndMode(project.getId(),
            getStartIntervalDate(infoInterval), LaunchModeEnum.DEFAULT
        );

    Map<String, ?> result;

    switch (widgetType) {
      case INVESTIGATED:
        result = dataConverter.getInvestigatedProjectInfo(launches, infoInterval);
        break;
      case CASES_STATISTIC:
        result = dataConverter.getTestCasesStatisticsProjectInfo(launches);
        break;
      case LAUNCHES_QUANTITY:
        result = dataConverter.getLaunchesQuantity(launches, infoInterval);
        break;
      case ISSUES_CHART:
        result = dataConverter.getLaunchesIssues(launches, infoInterval);
        break;
      case ACTIVITIES:
        result = getActivities(project, infoInterval);
        break;
      case LAST_LAUNCH:
        result = getLastLaunchStatistics(project.getId());
        break;
      default:
        // empty result
        result = Collections.emptyMap();
    }

    return result;
  }

  private Map<String, ?> getLastLaunchStatistics(Long projectId) {
    Optional<Launch> launchOptional = launchRepository.findLastRun(projectId, Mode.DEFAULT.name());
    return launchOptional.isPresent() ?
        Collections.singletonMap(RESULT, launchConverter.TO_RESOURCE.apply(launchOptional.get())) :
        Collections.emptyMap();
  }

  private Map<String, List<ActivityResource>> getActivities(Project project,
      InfoInterval infoInterval) {
    String value = Arrays.stream(ActivityAction.values()).filter(not(ACTIVITIES_PROJECT_FILTER))
        .map(ActivityAction::getValue).collect(joining(","));
    Filter filter = new Filter(Activity.class,
        Lists.newArrayList(new FilterCondition(IN, false, value, CRITERIA_EVENT_NAME),
            new FilterCondition(EQUALS, false, String.valueOf(project.getId()),
                CRITERIA_PROJECT_ID
            ), new FilterCondition(GREATER_THAN_OR_EQUALS, false,
                String.valueOf(Timestamp.valueOf(getStartIntervalDate(infoInterval)).getTime()),
                CRITERIA_CREATED_AT
            )
        )
    );
    List<Activity> activities = activityRepository.findByFilter(filter,
        PageRequest.of(0, LIMIT, Sort.by(Sort.Direction.DESC, CRITERIA_CREATED_AT))
    ).getContent();

    Map<Long, String> userIdLoginMapping = userRepository.findAllById(activities.stream()
            .filter(a -> a.getSubjectId() != null && a.getSubjectType() == EventSubject.USER)
            .map(Activity::getSubjectId).collect(Collectors.toSet())).stream()
        .collect(toMap(User::getId, User::getLogin));

    return Collections.singletonMap(RESULT, activities.stream().map(
            a -> ofNullable(a.getSubjectId()).map(
                    userId -> TO_RESOURCE_WITH_USER.apply(a, userIdLoginMapping.get(userId)))
                .orElseGet(() -> TO_RESOURCE.apply(a)))
        .peek(resource -> resource.setProjectName(project.getName())).collect(toList()));
  }
}
