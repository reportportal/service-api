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

package com.epam.reportportal.infrastructure.persistence.dao.util;

import static com.epam.reportportal.infrastructure.persistence.dao.constant.LogRepositoryConstants.LOG_LEVEL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.LogRepositoryConstants.PAGE_NUMBER;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.LogRepositoryConstants.TYPE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ID;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.ATTACHMENT_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.ATTRIBUTE_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.DASHBOARD_WIDGET_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.ITEM_ATTRIBUTE_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.LOG_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.ORGANIZATION_USER_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.PATTERN_TEMPLATE_NAME_RECORD_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.PROJECT_USER_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.TICKET_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.USER_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ACTIVITY;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.FILTER_SORT;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.INTEGRATION;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.LOG;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ORGANIZATION;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ORGANIZATION_USER;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.OWNED_ENTITY;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.PARAMETER;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.PROJECT_ATTRIBUTE;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JProject.PROJECT;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JProjectUser.PROJECT_USER;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTestItem.TEST_ITEM;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JUsers.USERS;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.infrastructure.persistence.entity.filter.FilterSort;
import com.epam.reportportal.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.infrastructure.persistence.entity.item.NestedItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.NestedItemPage;
import com.epam.reportportal.infrastructure.persistence.entity.item.Parameter;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationFilter;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationProfile;
import com.epam.reportportal.infrastructure.persistence.entity.pattern.PatternTemplateTestItem;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectProfile;
import com.epam.reportportal.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

/**
 * Fetches results from db by JOOQ queries into Java objects.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class ResultFetchers {

  private ResultFetchers() {
    //static only
  }

  /**
   * Fetches records from db results into list of {@link Project} objects.
   */
  public static final Function<Result<? extends Record>, List<Project>> PROJECT_FETCHER = rows -> {
    Map<Long, Project> projects = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(PROJECT.ID);
      Project project;
      if (!projects.containsKey(id)) {
        project = RecordMappers.PROJECT_MAPPER.map(row);
      } else {
        project = projects.get(id);
      }
      ofNullable(row.field(PROJECT_ATTRIBUTE.VALUE)).flatMap(f -> ofNullable(row.get(f)))
          .ifPresent(field -> project.getProjectAttributes()
              .add(new ProjectAttribute().withProject(project)
                  .withAttribute(ATTRIBUTE_MAPPER.map(row)).withValue(field)));
      ofNullable(row.field(PROJECT_USER.PROJECT_ROLE)).flatMap(f -> ofNullable(row.get(f)))
          .ifPresent(field -> {
            Set<ProjectUser> projectUsers = ofNullable(project.getUsers()).orElseGet(
                Sets::newHashSet);
            projectUsers.add(PROJECT_USER_MAPPER.map(row));
            project.setUsers(projectUsers);
          });

      projects.put(id, project);
    });
    return new ArrayList<>(projects.values());
  };

  /**
   * Fetches records from db results into list of {@link Organization} objects.
   */
  public static final Function<Result<? extends Record>, List<OrganizationProfile>> ORGANIZATION_FETCHER = rows -> {
    Map<Long, OrganizationProfile> orgs = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(ORGANIZATION.ID);
      OrganizationProfile organization;
      if (!orgs.containsKey(id)) {
        organization = OrganizationMapper.ORGANIZATION_MAPPER.map(row);
      } else {
        organization = orgs.get(id);
      }
      orgs.put(id, organization);
    });

    return new ArrayList<>(orgs.values());
  };


  /**
   * Fetches records from db results into list of {@link Launch} objects.
   */
  public static final Function<Result<? extends Record>, List<Launch>> LAUNCH_FETCHER = rows -> {
    Map<Long, Launch> launches = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(LAUNCH.ID);
      Launch launch;
      if (!launches.containsKey(id)) {
        launch = RecordMappers.LAUNCH_RECORD_MAPPER.map(row);
      } else {
        launch = launches.get(id);
      }
      ITEM_ATTRIBUTE_MAPPER.apply(row).ifPresent(it -> launch.getAttributes().addAll(it));
      launch.getStatistics().add(RecordMappers.STATISTICS_RECORD_MAPPER.map(row));
      launches.put(id, launch);
    });
    return new ArrayList<>(launches.values());
  };

  /**
   * Fetches records from db results into list of {@link TestItem} objects.
   */
  public static final Function<Result<? extends Record>, List<TestItem>> TEST_ITEM_FETCHER = rows -> {
    Map<Long, TestItem> testItems = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(TEST_ITEM.ITEM_ID);
      TestItem testItem;
      if (!testItems.containsKey(id)) {
        testItem = RecordMappers.TEST_ITEM_RECORD_MAPPER.map(row);
      } else {
        testItem = testItems.get(id);
      }
      ITEM_ATTRIBUTE_MAPPER.apply(row).ifPresent(it -> testItem.getAttributes().addAll(it));
      ofNullable(row.get(PARAMETER.ITEM_ID)).ifPresent(
          it -> testItem.getParameters().add(row.into(Parameter.class)));
      testItem.getItemResults().getStatistics()
          .add(RecordMappers.STATISTICS_RECORD_MAPPER.map(row));
      PATTERN_TEMPLATE_NAME_RECORD_MAPPER.apply(row)
          .ifPresent(patternTemplate -> testItem.getPatternTemplateTestItems()
              .add(new PatternTemplateTestItem(patternTemplate, testItem)));
      if (testItem.getItemResults().getIssue() != null) {
        TICKET_MAPPER.apply(row)
            .ifPresent(ticket -> testItem.getItemResults().getIssue().getTickets().add(ticket));
      }
      testItems.put(id, testItem);
    });
    return new ArrayList<>(testItems.values());
  };

  /**
   * Fetches records from db results into list of {@link TestItem} objects.
   */
  public static final Function<Result<? extends Record>, List<TestItem>> TEST_ITEM_CLIPPED_FETCHER = records -> {
    Map<Long, TestItem> testItems = Maps.newLinkedHashMap();
    records.forEach(record -> {
      Long id = record.get(TEST_ITEM.ITEM_ID);
      TestItem testItem;
      if (!testItems.containsKey(id)) {
        testItem = RecordMappers.TEST_ITEM_RECORD_MAPPER.map(record);
      } else {
        testItem = testItems.get(id);
      }
      testItem.getItemResults().getStatistics()
          .add(RecordMappers.STATISTICS_RECORD_MAPPER.map(record));
      testItem.getAttachments().add(RecordMappers.ATTACHMENT_MAPPER.map(record));
      testItems.put(id, testItem);
    });
    return new ArrayList<>(testItems.values());
  };

  /**
   * Fetches records from db results into list of {@link TestItem} objects.
   */
  public static final Function<Result<? extends Record>, List<TestItem>> TEST_ITEM_RETRY_FETCHER = rows -> {
    Map<Long, TestItem> testItems = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(TEST_ITEM.ITEM_ID);
      TestItem testItem = testItems.computeIfAbsent(id,
          key -> RecordMappers.TEST_ITEM_RECORD_MAPPER.map(row));
      ofNullable(row.field(PARAMETER.ITEM_ID)).ifPresent(
          it -> testItem.getParameters().add(row.into(Parameter.class)));
      testItems.put(id, testItem);
    });
    return new ArrayList<>(testItems.values());
  };

  /**
   * Fetches records from db results into list of {@link Log} objects.
   */
  public static final Function<Result<? extends Record>, List<Log>> LOG_FETCHER = rows -> {
    Map<Long, Log> logs = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(LOG.ID);
      if (!logs.containsKey(id)) {
        logs.put(id, LOG_MAPPER.apply(row, ATTACHMENT_MAPPER));
      }
    });
    return new ArrayList<>(logs.values());
  };

  /**
   * Fetches records from db results into list of {@link Activity} objects.
   */
  public static final Function<Result<? extends Record>, List<Activity>> ACTIVITY_FETCHER = rows -> {
    Map<Long, Activity> activities = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(ACTIVITY.ID);
      Activity activity;
      if (!activities.containsKey(id)) {
        activity = RecordMappers.ACTIVITY_MAPPER.map(row);
      } else {
        activity = activities.get(id);
      }
      activities.put(id, activity);
    });
    return new ArrayList<>(activities.values());
  };

  /**
   * Fetches records from db results into list of {@link Integration} objects.
   */
  public static final Function<Result<? extends Record>, List<Integration>> INTEGRATION_FETCHER = rows -> {
    Map<Integer, Integration> integrations = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Integer id = row.get(INTEGRATION.ID);
      Integration integration;
      if (!integrations.containsKey(id)) {
        integration = row.into(Integration.class);
      } else {
        integration = integrations.get(id);
      }
      integrations.put(id, integration);
    });
    return new ArrayList<>(integrations.values());
  };

  public static final Function<Result<? extends Record>, List<User>> USER_FETCHER = rows -> {
    Map<Long, User> users = Maps.newLinkedHashMap();
    rows.forEach(row -> {
      Long id = row.get(USERS.ID);
      User user;
      if (!users.containsKey(id)) {
        user = row.map(USER_MAPPER);
      } else {
        user = users.get(id);
      }
      if (ofNullable(row.get(PROJECT_USER.PROJECT_ROLE)).isPresent()) {
        boolean isProjectAdded = user.getProjects().stream()
            .map(ProjectUser::getProject)
            .map(Project::getKey)
            .anyMatch(prjKey -> prjKey.equals(row.get(PROJECT.KEY)));
        if (!isProjectAdded) {
          user.getProjects().add(PROJECT_USER_MAPPER.map(row));
        }
      }

      if (ofNullable(row.get(ORGANIZATION_USER.ORGANIZATION_ROLE)).isPresent()) {
        boolean isOrgAdded = user.getOrganizationUsers().stream()
            .map(OrganizationUser::getOrganization)
            .map(Organization::getId)
            .anyMatch(orgId -> orgId.equals(row.get(ORGANIZATION.ID)));
        if (!isOrgAdded) {
          user.getOrganizationUsers()
              .add(ORGANIZATION_USER_MAPPER.map(row));
        }
      }

      users.put(id, user);
    });
    return new ArrayList<>(users.values());
  };

  public static final Function<Result<? extends Record>, List<User>> USER_WITHOUT_PROJECT_FETCHER = rows -> {
    Map<Long, User> users = Maps.newLinkedHashMap();
    rows.forEach(
        row -> users.computeIfAbsent(row.get(USERS.ID), key -> row.map(USER_MAPPER)));
    return new ArrayList<>(users.values());
  };

  public static final Function<Result<? extends Record>, List<UserFilter>> USER_FILTER_FETCHER = result -> {
    Map<Long, UserFilter> userFilterMap = Maps.newLinkedHashMap();
    result.forEach(r -> {
      Long userFilterID = r.get(ID, Long.class);
      UserFilter userFilter;
      if (userFilterMap.containsKey(userFilterID)) {
        userFilter = userFilterMap.get(userFilterID);
      } else {
        userFilter = r.into(UserFilter.class);
        userFilter.setOwner(r.get(OWNED_ENTITY.OWNER));
        Project project = new Project();
        project.setId(r.get(OWNED_ENTITY.PROJECT_ID, Long.class));
        userFilter.setProject(project);
      }
      userFilter.getFilterCondition().add(r.into(FilterCondition.class));
      FilterSort filterSort = new FilterSort();
      filterSort.setId(r.get(FILTER_SORT.ID));
      filterSort.setField(r.get(FILTER_SORT.FIELD));
      filterSort.setDirection(Sort.Direction.valueOf(r.get(FILTER_SORT.DIRECTION).toString()));
      userFilter.getFilterSorts().add(filterSort);
      userFilterMap.put(userFilterID, userFilter);
    });
    return Lists.newArrayList(userFilterMap.values());
  };

  public static final Function<Result<? extends Record>, List<Dashboard>> DASHBOARD_FETCHER = result -> {
    Map<Long, Dashboard> dashboardMap = Maps.newLinkedHashMap();
    result.forEach(r -> {
      Long dashboardId = r.get(ID, Long.class);
      Dashboard dashboard;
      if (dashboardMap.containsKey(dashboardId)) {
        dashboard = dashboardMap.get(dashboardId);
      } else {
        dashboard = r.into(Dashboard.class);
        dashboard.setOwner(r.get(OWNED_ENTITY.OWNER));
        Project project = new Project();
        project.setId(r.get(OWNED_ENTITY.PROJECT_ID, Long.class));
        dashboard.setProject(project);
      }
      DASHBOARD_WIDGET_MAPPER.apply(r).ifPresent(dashboard::addWidget);
      dashboardMap.put(dashboardId, dashboard);
    });
    return Lists.newArrayList(dashboardMap.values());
  };

  public static final Function<Result<? extends Record>, List<Widget>> WIDGET_FETCHER = result -> {
    Map<Long, Widget> widgetMap = Maps.newLinkedHashMap();
    result.forEach(r -> {
      Long widgetId = r.get(ID, Long.class);
      Widget widget;
      if (widgetMap.containsKey(widgetId)) {
        widget = widgetMap.get(widgetId);
      } else {
        widget = r.into(Widget.class);
        widget.setOwner(r.get(OWNED_ENTITY.OWNER));
        Project project = new Project();
        project.setId(r.get(OWNED_ENTITY.PROJECT_ID, Long.class));
        widget.setProject(project);
      }
      widgetMap.put(widgetId, widget);
    });
    return Lists.newArrayList(widgetMap.values());
  };

  public static final Function<Result<? extends Record>, List<NestedItem>> NESTED_ITEM_FETCHER = result -> {
    List<NestedItem> nestedItems = Lists.newArrayListWithExpectedSize(result.size());
    result.forEach(row -> nestedItems.add(new NestedItem(
        row.get(ID, Long.class),
        row.get(TYPE, String.class),
        row.get(LOG_LEVEL, Integer.class)
    )));
    return nestedItems;
  };

  public static final Function<Result<? extends Record>, List<NestedItemPage>> NESTED_ITEM_LOCATED_FETCHER = result -> {
    List<NestedItemPage> itemWithLocation = Lists.newArrayListWithExpectedSize(result.size());
    result.forEach(row -> itemWithLocation.add(new NestedItemPage(row.get(ID, Long.class),
        row.get(TYPE, String.class),
        row.get(LOG_LEVEL, Integer.class),
        row.get(PAGE_NUMBER, Integer.class)
    )));
    return itemWithLocation;
  };

  public static final Function<Result<? extends Record>, ReportPortalUser> REPORTPORTAL_USER_FETCHER = rows -> {
    if (!CollectionUtils.isEmpty(rows)) {
      ReportPortalUser user = ReportPortalUser.userBuilder()
          .withUserName(rows.get(0).get(USERS.LOGIN))
          .withActive(rows.get(0).get(USERS.ACTIVE))
          .withPassword(ofNullable(rows.get(0).get(USERS.PASSWORD)).orElse(""))
          .withAuthorities(Collections.emptyList())
          .withUserId(rows.get(0).get(USERS.ID))
          .withUserRole(UserRole.findByName(rows.get(0).get(USERS.ROLE))
              .orElseThrow(
                  () -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR)))
          .withOrganizationDetails(new HashMap<>())
          .withEmail(rows.get(0).get(USERS.EMAIL))
          .build();

      rows.forEach(
          row ->
              ofNullable(row.get(ORGANIZATION_USER.ORGANIZATION_ID))
                  .ifPresent(orgId -> {
                    String orgName = row.get(ORGANIZATION.NAME, String.class);

                    if (!user.getOrganizationDetails().containsKey(orgName)) {
                      ReportPortalUser.OrganizationDetails organizationDetails =
                          ReportPortalUser.OrganizationDetails.builder()
                              .withOrgId(row.get(ORGANIZATION_USER.ORGANIZATION_ID, Long.class))
                              .withOrgName(orgName)
                              .withOrganizationRole(
                                  row.get(ORGANIZATION_USER.ORGANIZATION_ROLE, String.class))
                              .withProjectDetails(new HashMap<>())
                              .build();
                      user.getOrganizationDetails().put(orgName, organizationDetails);
                    }

                    ofNullable(row.get(PROJECT_USER.PROJECT_ID, Long.class))
                        .ifPresent(projectId -> {
                          String projectKey = row.get(PROJECT.KEY, String.class);
                          ReportPortalUser.OrganizationDetails.ProjectDetails projectDetails =
                              ReportPortalUser.OrganizationDetails.ProjectDetails.builder()
                                  .withProjectId(projectId)
                                  .withProjectKey(projectKey)
                                  .withProjectRole(row.get(PROJECT_USER.PROJECT_ROLE, String.class))
                                  .build();

                          user.getOrganizationDetails().get(orgName)
                              .getProjectDetails()
                              .put(projectKey, projectDetails);

                        });
                  }));

      return user;
    }
    return null;
  };

  public static final Function<Result<? extends Record>, List<ProjectProfile>> ORGANIZATION_PROJECT_LIST_FETCHER = rows -> {
    List<ProjectProfile> projectProfiles = new ArrayList<>(rows.size());

    rows.forEach(row -> {
      ProjectProfile projectProfile = new ProjectProfile();

      projectProfile.setId(row.get(PROJECT.ID));
      projectProfile.setOrganizationId(row.get(PROJECT.ORGANIZATION_ID));
      projectProfile.setCreatedAt(row.get(PROJECT.CREATED_AT, Instant.class));
      projectProfile.setUpdatedAt(row.get(PROJECT.UPDATED_AT, Instant.class));
      projectProfile.setKey(row.get(PROJECT.KEY));
      projectProfile.setSlug(row.get(PROJECT.SLUG));
      projectProfile.setName(row.get(PROJECT.NAME, String.class));

      projectProfile.setLaunchesQuantity(row.get(OrganizationFilter.LAUNCHES_QUANTITY, Integer.class));
      projectProfile.setLastRun(row.get(OrganizationFilter.LAST_RUN, Instant.class));
      projectProfile.setUsersQuantity(row.get(OrganizationFilter.USERS_QUANTITY, Integer.class));

      projectProfiles.add(projectProfile);

    });

    return projectProfiles;
  };

}
