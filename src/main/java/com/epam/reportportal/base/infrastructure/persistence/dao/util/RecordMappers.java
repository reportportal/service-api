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

package com.epam.reportportal.base.infrastructure.persistence.dao.util;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget.ATTRIBUTE_ALIAS;
import static com.epam.reportportal.base.infrastructure.persistence.dao.LogRepositoryCustomImpl.ROOT_ITEM_ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestItemRepositoryConstants.ATTACHMENTS_COUNT;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestItemRepositoryConstants.HAS_CONTENT;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMapperUtils.fieldExcludingPredicate;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.CLUSTERS;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.CONTENT_FIELD;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.DASHBOARD;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.DASHBOARD_WIDGET;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.FILTER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.INTEGRATION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.INTEGRATION_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_GROUP;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LOG;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION_USER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PATTERN_TEMPLATE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT_USER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.STATISTICS_FIELD;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TEST_ITEM_RESULTS;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TICKET;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_EXECUTION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_EXECUTION_COMMENT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_FOLDER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_FOLDER_TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_PLAN;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.WIDGET;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JActivity.ACTIVITY;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JAttachment.ATTACHMENT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JUsers.USERS;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.model.analyzer.IndexLaunch;
import com.epam.reportportal.base.infrastructure.model.analyzer.IndexLog;
import com.epam.reportportal.base.infrastructure.model.analyzer.IndexTestItem;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.OwnedEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.attribute.Attribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.bts.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.DashboardWidget;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.DashboardWidgetId;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.NestedStep;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.pattern.PatternTemplate;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.Statistics;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUserId;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.persistence.jooq.Tables;
import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JLog;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.jooq.Field;
import org.jooq.JSON;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.Result;

/**
 * Set of record mappers that helps to convert the result of jooq queries into Java objects
 *
 * @author Pavel Bortnik
 */
public class RecordMappers {

  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * Maps record into {@link Attribute} object
   */
  public static final RecordMapper<? super Record, Attribute> ATTRIBUTE_MAPPER = record -> {
    Attribute attribute = new Attribute();
    ofNullable(record.field(ATTRIBUTE.ID)).ifPresent(f -> attribute.setId(record.get(f)));
    ofNullable(record.field(ATTRIBUTE.NAME)).ifPresent(f -> attribute.setName(record.get(f)));

    return attribute;
  };

  public static final RecordMapper<? super Record, IssueGroup> ISSUE_GROUP_RECORD_MAPPER = r -> {
    if (r.field(ISSUE_GROUP.ISSUE_GROUP_ID) == null || r.get(ISSUE_GROUP.ISSUE_GROUP_ID) == null) {
      return null;
    }
    IssueGroup issueGroup = new IssueGroup();
    ofNullable(r.get(ISSUE_GROUP.ISSUE_GROUP_ID))
        .ifPresent(igId -> issueGroup.setId(r.get(ISSUE_GROUP.ISSUE_GROUP_ID, Integer.class)));

    if (r.field(ISSUE_GROUP.ISSUE_GROUP_) != null && r.get(ISSUE_GROUP.ISSUE_GROUP_) != null) {
      issueGroup.setTestItemIssueGroup(
          TestItemIssueGroup.valueOf(r.get(ISSUE_GROUP.ISSUE_GROUP_).getLiteral()));
    }

    return issueGroup;
  };

  /**
   * Maps record into {@link IssueType} object
   */
  public static final RecordMapper<? super Record, IssueType> ISSUE_TYPE_RECORD_MAPPER = r -> {
    if (r.field(ISSUE_TYPE.ID) == null || r.get(ISSUE_TYPE.ID) == null) {
      return null;
    }
    IssueType type = new IssueType();
    ofNullable(r.get(ISSUE_TYPE.ID))
        .ifPresent(val -> type.setId(r.get(ISSUE_TYPE.ID)));
    ofNullable(r.get(ISSUE_TYPE.ISSUE_NAME))
        .ifPresent(longName -> type.setLongName(r.get(ISSUE_TYPE.ISSUE_NAME)));
    ofNullable(r.get(ISSUE_TYPE.LOCATOR))
        .ifPresent(locator -> type.setLocator(r.get(ISSUE_TYPE.LOCATOR)));
    ofNullable(r.get(ISSUE_TYPE.ABBREVIATION))
        .ifPresent(shortName -> type.setShortName(r.get(ISSUE_TYPE.ABBREVIATION)));
    ofNullable(r.get(ISSUE_TYPE.HEX_COLOR))
        .ifPresent(hexColor -> type.setHexColor(r.get(ISSUE_TYPE.HEX_COLOR)));
    type.setIssueGroup(ISSUE_GROUP_RECORD_MAPPER.map(r));

    return type;
  };

  /**
   * Maps record into {@link IssueEntity} object
   */
  public static final RecordMapper<? super Record, IssueEntity> ISSUE_RECORD_MAPPER = r -> {
    if (r.field(ISSUE.ISSUE_ID) == null || r.get(ISSUE.ISSUE_ID) == null) {
      return null;
    }
    IssueEntity issueEntity = r.into(IssueEntity.class);
    issueEntity.setIssueType(ISSUE_TYPE_RECORD_MAPPER.map(r));
    return issueEntity;
  };


  /**
   * Maps record into {@link Project} object
   */
  public static final RecordMapper<? super Record, Project> PROJECT_MAPPER = r -> {
    Project project = r.into(PROJECT.ID, PROJECT.NAME, PROJECT.ORGANIZATION, PROJECT.CREATED_AT,
            PROJECT.ORGANIZATION_ID)
        .into(Project.class);
    ofNullable(r.field(PROJECT.KEY))
        .ifPresent(f -> project.setKey(r.get(PROJECT.KEY)));
    ofNullable(r.field(PROJECT.SLUG))
        .ifPresent(f -> project.setSlug(r.get(PROJECT.SLUG)));

    ofNullable(r.field(PROJECT.METADATA)).ifPresent(f -> {
      String metaDataString = r.get(f, String.class);
      ofNullable(metaDataString).ifPresent(md -> {
        try {
          Metadata metadata = objectMapper.readValue(metaDataString, Metadata.class);
          project.setMetadata(metadata);
        } catch (IOException e) {
          throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
              "Error during parsing user metadata");
        }
      });
    });

    return project;
  };


  /**
   * Maps record into {@link TestItemResults} object
   */
  public static final RecordMapper<? super Record, TestItemResults> TEST_ITEM_RESULTS_RECORD_MAPPER = r -> {
    TestItemResults results = r.into(TestItemResults.class);
    results.setIssue(ISSUE_RECORD_MAPPER.map(r));
    return results;
  };

  public static final RecordMapper<? super Record, Statistics> STATISTICS_RECORD_MAPPER = r -> {
    Statistics statistics = new Statistics();

    StatisticsField statisticsField = new StatisticsField();
    statisticsField.setName(r.get(STATISTICS_FIELD.NAME));

    statistics.setStatisticsField(statisticsField);
    statistics.setCounter(ofNullable(r.get(Tables.STATISTICS.S_COUNTER)).orElse(0));
    return statistics;
  };

  public static final RecordMapper<? super Record, Attachment> ATTACHMENT_MAPPER = r -> ofNullable(
      r.get(ATTACHMENT.ID)).map(id -> {
    Attachment attachment = new Attachment();
    attachment.setId(id);
    attachment.setFileId(r.get(ATTACHMENT.FILE_ID));
    attachment.setThumbnailId(r.get(ATTACHMENT.THUMBNAIL_ID));
    attachment.setContentType(r.get(ATTACHMENT.CONTENT_TYPE));
    attachment.setFileSize(r.get(ATTACHMENT.FILE_SIZE));
    attachment.setProjectId(r.get(ATTACHMENT.PROJECT_ID));
    attachment.setLaunchId(r.get(ATTACHMENT.LAUNCH_ID));
    attachment.setItemId(r.get(ATTACHMENT.ITEM_ID));
    attachment.setFileName(r.get(ATTACHMENT.FILE_NAME));

    return attachment;
  }).orElse(null);

  private static final RecordMapper<? super Record, Log> COMMON_LOG_RECORD_MAPPER = result -> {
    Log log = new Log();
    log.setId(result.get(LOG.ID, Long.class));
    log.setLogTime(result.get(LOG.LOG_TIME, Instant.class));
    log.setLogMessage(result.get(LOG.LOG_MESSAGE, String.class));
    log.setLastModified(result.get(LOG.LAST_MODIFIED, Instant.class));
    log.setLogLevel(result.get(JLog.LOG.LOG_LEVEL, Integer.class));
    log.setProjectId(result.get(LOG.PROJECT_ID, Long.class));
    ofNullable(result.get(LOG.LAUNCH_ID)).map(Launch::new).ifPresent(log::setLaunch);
    return log;
  };

  public static final RecordMapper<? super Record, Log> LOG_RECORD_MAPPER = result -> {
    Log log = COMMON_LOG_RECORD_MAPPER.map(result);
    ofNullable(result.get(LOG.ITEM_ID)).map(TestItem::new).ifPresent(log::setTestItem);
    return log;
  };

  public static final RecordMapper<? super Record, Log> LOG_UNDER_RECORD_MAPPER = result -> {
    Log log = COMMON_LOG_RECORD_MAPPER.map(result);
    ofNullable(result.get(ROOT_ITEM_ID, Long.class)).map(TestItem::new).ifPresent(log::setTestItem);
    log.setClusterId(result.get(LOG.CLUSTER_ID, Long.class));
    return log;
  };

  public static final Function<Result<? extends Record>, Map<Long, List<IndexLog>>> INDEX_LOG_FETCHER = result -> {
    final Map<Long, List<IndexLog>> indexLogMapping = new HashMap<>();
    result.forEach(r -> {
      final Long itemId = r.get(ROOT_ITEM_ID, Long.class);

      final IndexLog indexLog = new IndexLog();
      indexLog.setLogId(r.get(LOG.ID, Long.class));
      indexLog.setMessage(r.get(LOG.LOG_MESSAGE, String.class));
      indexLog.setLogLevel(r.get(JLog.LOG.LOG_LEVEL, Integer.class));
      indexLog.setLogTime(r.get(LOG.LOG_TIME, LocalDateTime.class));
      indexLog.setClusterId(r.get(CLUSTERS.INDEX_ID));

      ofNullable(indexLogMapping.get(itemId)).ifPresentOrElse(indexLogs -> indexLogs.add(indexLog),
          () -> {
            final List<IndexLog> indexLogs = new ArrayList<>();
            indexLogs.add(indexLog);
            indexLogMapping.put(itemId, indexLogs);
          });
    });
    return indexLogMapping;
  };

  public static final BiFunction<? super Record, RecordMapper<? super Record, Attachment>, Log> LOG_MAPPER = (result, attachmentMapper) -> {
    Log log = LOG_RECORD_MAPPER.map(result);
    log.setAttachment(attachmentMapper.map(result));
    return log;
  };

  public static final BiFunction<? super Record, RecordMapper<? super Record, Attachment>, Log> LOG_UNDER_MAPPER = (result, attachmentMapper) -> {
    Log log = LOG_UNDER_RECORD_MAPPER.map(result);
    log.setAttachment(attachmentMapper.map(result));
    return log;
  };

  /**
   * Maps record into {@link TestItem} object
   */
  public static final RecordMapper<? super Record, TestItem> TEST_ITEM_RECORD_MAPPER = r -> {
    TestItem testItem = r.into(TestItem.class);
    testItem.setItemId(r.get(TEST_ITEM.ITEM_ID));
    testItem.setName(r.get(TEST_ITEM.NAME));
    testItem.setCodeRef(r.get(TEST_ITEM.CODE_REF));
    testItem.setItemResults(TEST_ITEM_RESULTS_RECORD_MAPPER.map(r));
    ofNullable(r.get(TEST_ITEM.LAUNCH_ID)).ifPresent(testItem::setLaunchId);
    ofNullable(r.get(TEST_ITEM.PARENT_ID)).ifPresent(testItem::setParentId);
    return testItem;
  };

  public static final RecordMapper<? super Record, IndexTestItem> INDEX_TEST_ITEM_RECORD_MAPPER = record -> {
    final IndexTestItem indexTestItem = new IndexTestItem();
    indexTestItem.setTestItemId(record.get(TEST_ITEM.ITEM_ID));
    indexTestItem.setTestItemName(record.get(TEST_ITEM.NAME));
    indexTestItem.setStartTime(record.get(TEST_ITEM.START_TIME, LocalDateTime.class));
    indexTestItem.setUniqueId(record.get(TEST_ITEM.UNIQUE_ID));
    indexTestItem.setTestCaseHash(record.get(TEST_ITEM.TEST_CASE_HASH));
    indexTestItem.setAutoAnalyzed(record.get(ISSUE.AUTO_ANALYZED));
    indexTestItem.setIssueTypeLocator(record.get(ISSUE_TYPE.LOCATOR));
    return indexTestItem;
  };

  public static final RecordMapper<? super Record, NestedStep> NESTED_STEP_RECORD_MAPPER = r -> new NestedStep(
      r.get(TEST_ITEM.ITEM_ID),
      r.get(TEST_ITEM.NAME),
      r.get(TEST_ITEM.UUID),
      TestItemTypeEnum.valueOf(r.get(TEST_ITEM.TYPE).getLiteral()),
      r.get(HAS_CONTENT, Boolean.class),
      r.get(ATTACHMENTS_COUNT, Integer.class),
      StatusEnum.valueOf(r.get(TEST_ITEM_RESULTS.STATUS).getLiteral()),
      r.get(TEST_ITEM.START_TIME, Instant.class),
      r.get(TEST_ITEM_RESULTS.END_TIME, Instant.class),
      r.get(TEST_ITEM_RESULTS.DURATION)
  );

  /**
   * Maps record into {@link PatternTemplate} object (only {@link PatternTemplate#id} and {@link PatternTemplate#name}
   * fields)
   */
  public static final Function<? super Record, Optional<PatternTemplate>> PATTERN_TEMPLATE_NAME_RECORD_MAPPER = r -> ofNullable(
      r.get(
          PATTERN_TEMPLATE.NAME)).map(name -> {
    PatternTemplate patternTemplate = new PatternTemplate();
    patternTemplate.setId(r.get(PATTERN_TEMPLATE.ID));
    patternTemplate.setName(name);
    return patternTemplate;
  });

  /**
   * Maps record into {@link Launch} object
   */
  public static final RecordMapper<? super Record, Launch> LAUNCH_RECORD_MAPPER = r -> {
    Launch launch = r.into(Launch.class);
    launch.setId(r.get(LAUNCH.ID));
    launch.setName(r.get(LAUNCH.NAME));
    launch.setUserId(r.get(LAUNCH.USER_ID));
    launch.setRetentionPolicy(r.into(LAUNCH.RETENTION_POLICY).into(RetentionPolicyEnum.class));
    return launch;
  };

  public static final RecordMapper<? super Record, IndexLaunch> INDEX_LAUNCH_RECORD_MAPPER = row -> {
    final IndexLaunch indexLaunch = new IndexLaunch();
    indexLaunch.setLaunchId(row.get(LAUNCH.ID));
    indexLaunch.setLaunchName(row.get(LAUNCH.NAME));
    indexLaunch.setLaunchStartTime(row.get(LAUNCH.START_TIME, LocalDateTime.class));
    indexLaunch.setProjectId(row.get(LAUNCH.PROJECT_ID));
    indexLaunch.setLaunchNumber(
        (row.get(LAUNCH.NUMBER) != null) ? row.get(LAUNCH.NUMBER).longValue() : null);
    return indexLaunch;
  };

  public static final RecordMapper<Record, ReportPortalUser> REPORT_PORTAL_USER_MAPPER = r -> ReportPortalUser.userBuilder()
      .withActive(r.get(USERS.ACTIVE))
      .withUserName(r.get(USERS.LOGIN))
      .withPassword(ofNullable(r.get(USERS.PASSWORD)).orElse(""))
      .withAuthorities(Collections.emptyList())
      .withUserId(r.get(USERS.ID))
      .withUserRole(UserRole.findByName(r.get(USERS.ROLE))
          .orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR)))
      .withEmail(r.get(USERS.EMAIL))
      .build();

  public static final RecordMapper<Record, User> USER_MAPPER = r -> {
    User user = r.into(
            USERS.fieldStream().filter(f -> fieldExcludingPredicate(USERS.METADATA).test(f))
                .toArray(Field[]::new))
        .into(User.class);
    String metaDataString = r.get(USERS.METADATA, String.class);
    ofNullable(metaDataString).ifPresent(md -> {
      try {
        Metadata metadata = objectMapper.readValue(metaDataString, Metadata.class);
        user.setMetadata(metadata);
      } catch (IOException e) {
        throw new ReportPortalException("Error during parsing user metadata");
      }
    });
    return user;
  };

  public static final RecordMapper<Record, ProjectUser> PROJECT_USER_MAPPER = r -> {
    ProjectUser projectUser = new ProjectUser();
    projectUser.setProjectRole(r.into(PROJECT_USER.PROJECT_ROLE).into(ProjectRole.class));

    Project project = new Project();
    project.setId(r.get(PROJECT_USER.PROJECT_ID));
    project.setName(r.get(PROJECT.NAME));
    project.setKey(r.get(PROJECT.KEY));
    project.setSlug(r.get(PROJECT.SLUG));
    project.setOrganizationId(r.get(ORGANIZATION.ID));

    User user = new User();
    user.setLogin(r.get(USERS.LOGIN));
    user.setId(r.get(PROJECT_USER.PROJECT_ID));

    projectUser.setProject(project);
    projectUser.setUser(user);
    return projectUser;
  };

  public static final RecordMapper<Record, OrganizationUser> ORGANIZATION_USER_MAPPER = r -> {
    OrganizationUser orgUser = new OrganizationUser();

    OrganizationUserId organizationUserId = new OrganizationUserId();
    organizationUserId.setOrganizationId(r.get(ORGANIZATION.ID));
    organizationUserId.setUserId(r.get(USERS.ID));

    Organization organization = new Organization();
    organization.setId(r.get(ORGANIZATION.ID));
    organization.setSlug(r.get(ORGANIZATION.SLUG));
    organization.setName(r.get(ORGANIZATION.NAME));

    orgUser.setId(organizationUserId);
    orgUser.setOrganizationRole(r.into(ORGANIZATION_USER.ORGANIZATION_ROLE).into(OrganizationRole.class));
    orgUser.setOrganization(organization);
    return orgUser;
  };

  public static final RecordMapper<Record, MembershipDetails> ASSIGNMENT_DETAILS_MAPPER = r -> {
    MembershipDetails md = new MembershipDetails();

    ofNullable(r.get(PROJECT.ORGANIZATION_ID)).ifPresent(md::setOrgId);
    ofNullable(r.get(ORGANIZATION.NAME)).ifPresent(md::setOrgName);
    ofNullable(r.get(ORGANIZATION_USER.ORGANIZATION_ROLE))
        .ifPresent(orgRole -> md.setOrgRole(r.into(ORGANIZATION_USER.ORGANIZATION_ROLE)
            .into(OrganizationRole.class)));
    ofNullable(r.get(PROJECT.ID))
        .ifPresent(md::setProjectId);
    ofNullable(r.get(PROJECT.NAME)).ifPresent(md::setProjectName);
    ofNullable(r.into(PROJECT_USER.PROJECT_ROLE).into(ProjectRole.class))
        .ifPresent(projectRole -> md.setProjectRole(r.into(PROJECT_USER.PROJECT_ROLE)
            .into(ProjectRole.class)));
    ofNullable(r.get(PROJECT.KEY)).ifPresent(md::setProjectKey);
    ofNullable(r.get(PROJECT.SLUG)).ifPresent(md::setProjectSlug);

    return md;
  };

  public static final RecordMapper<? super Record, Activity> ACTIVITY_MAPPER = r -> {
    Activity activity = new Activity();
    activity.setId(r.get(ACTIVITY.ID));
    activity.setCreatedAt(r.get(ACTIVITY.CREATED_AT, Instant.class));
    activity.setAction(EventAction.valueOf(r.get(ACTIVITY.ACTION)));
    activity.setEventName(r.get(ACTIVITY.EVENT_NAME));
    activity.setPriority(EventPriority.valueOf(r.get(ACTIVITY.PRIORITY)));
    activity.setObjectId(r.get(ACTIVITY.OBJECT_ID));
    activity.setObjectName(r.get(ACTIVITY.OBJECT_NAME));
    activity.setObjectType(EventObject.valueOf(r.get(ACTIVITY.OBJECT_TYPE)));
    activity.setProjectId(r.get(ACTIVITY.PROJECT_ID));
    activity.setProjectName(r.get(PROJECT.NAME));
    String detailsJson = r.get(ACTIVITY.DETAILS, String.class);
    ofNullable(detailsJson).ifPresent(s -> {
      try {
        ActivityDetails details = objectMapper.readValue(s, ActivityDetails.class);
        activity.setDetails(details);
      } catch (IOException e) {
        throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR);
      }
    });
    activity.setSubjectId(r.get(ACTIVITY.SUBJECT_ID));
    activity.setSubjectName(ofNullable(r.get(USERS.LOGIN)).orElse(r.get(ACTIVITY.SUBJECT_NAME)));
    activity.setSubjectType(EventSubject.valueOf(r.get(ACTIVITY.SUBJECT_TYPE)));
    return activity;
  };

  public static final RecordMapper<? super Record, OwnedEntity> OWNED_ENTITY_RECORD_MAPPER = r -> r.into(
      OwnedEntity.class);

  private static final BiConsumer<Widget, ? super Record> WIDGET_USER_FILTER_MAPPER = (widget, res) -> ofNullable(
      res.get(FILTER.ID)).ifPresent(
      id -> {
        Set<UserFilter> filters = ofNullable(widget.getFilters()).orElseGet(Sets::newLinkedHashSet);
        UserFilter filter = new UserFilter();
        filter.setId(id);
        filters.add(filter);
        widget.setFilters(filters);
      });

  private static final BiConsumer<Widget, ? super Record> WIDGET_OPTIONS_MAPPER = (widget, res) -> {
    ofNullable(res.get(WIDGET.WIDGET_OPTIONS, String.class)).ifPresent(wo -> {
      try {
        WidgetOptions widgetOptions = objectMapper.readValue(wo, WidgetOptions.class);
        widget.setWidgetOptions(widgetOptions);
      } catch (IOException e) {
        throw new ReportPortalException("Error during parsing widget options");
      }
    });
  };

  private static final BiConsumer<Widget, ? super Record> WIDGET_CONTENT_FIELD_MAPPER = (widget, res) -> ofNullable(
      res.get(CONTENT_FIELD.FIELD)).ifPresent(
      field -> {
        Set<String> contentFields = ofNullable(widget.getContentFields()).orElseGet(
            Sets::newLinkedHashSet);
        contentFields.add(field);
        widget.setContentFields(contentFields);
      });

  public static final RecordMapper<? super Record, Widget> WIDGET_RECORD_MAPPER = r -> {
    Widget widget = new Widget();
    widget.setDescription(r.get(WIDGET.DESCRIPTION));
    widget.setId(r.get(WIDGET.ID));
    widget.setName(r.get(WIDGET.NAME));
    widget.setItemsCount(r.get(WIDGET.ITEMS_COUNT));
    widget.setWidgetType(r.get(WIDGET.WIDGET_TYPE));

    WIDGET_USER_FILTER_MAPPER.accept(widget, r);
    WIDGET_OPTIONS_MAPPER.accept(widget, r);
    WIDGET_CONTENT_FIELD_MAPPER.accept(widget, r);

    return widget;
  };

  public static final Function<Result<? extends Record>, List<Widget>> WIDGET_FETCHER = result -> {
    Map<Long, Widget> widgetMap = Maps.newLinkedHashMap();
    result.forEach(res -> {
      Long widgetId = res.get(WIDGET.ID);
      Widget widget = widgetMap.get(widgetId);
      if (ofNullable(widget).isPresent()) {
        WIDGET_USER_FILTER_MAPPER.accept(widget, res);
        WIDGET_OPTIONS_MAPPER.accept(widget, res);
        WIDGET_CONTENT_FIELD_MAPPER.accept(widget, res);
      } else {
        widgetMap.put(widgetId, WIDGET_RECORD_MAPPER.map(res));
      }
    });

    return Lists.newArrayList(widgetMap.values());
  };

  public static final Function<? super Record, Optional<List<ItemAttribute>>> ITEM_ATTRIBUTE_MAPPER = r -> {
    List<ItemAttribute> attributeList = new ArrayList<>();

    if (r.get(ATTRIBUTE_ALIAS) != null) {
      List<JSON> attributesArray = r.get(ATTRIBUTE_ALIAS, List.class);
      Gson gson = new Gson();
      Type listType = new TypeToken<List<String>>() {
      }.getType();

      for (JSON attributeEntry : attributesArray) {
        if (attributeEntry == null) {
          continue;
        }
        String[] attributes = gson.<List<String>>fromJson(attributeEntry.data(), listType)
            .toArray(new String[0]);

        if (attributes.length > 1 && (Strings.isNotEmpty(attributes[0])
            || Strings.isNotEmpty(attributes[1]))) {
          Boolean systemAttribute;
          //Case when system attribute is retrieved as 't' or 'f'
          if ("t".equals(attributes[2]) || "f".equals(attributes[2])) {
            systemAttribute = "t".equals(attributes[2]) ? Boolean.TRUE : Boolean.FALSE;
          } else {
            systemAttribute = Boolean.parseBoolean(attributes[2]);
          }
          attributeList.add(
              new ItemAttribute(attributes[0], attributes[1], systemAttribute)
          );
        }
      }
    }

    if (CollectionUtils.isNotEmpty(attributeList)) {
      return Optional.of(attributeList);
    } else {
      return Optional.empty();
    }
  };

  public static final Function<? super Record, Optional<Ticket>> TICKET_MAPPER = r -> {
    String ticketId = r.get(TICKET.TICKET_ID);
    if (ticketId != null) {
      return Optional.of(r.into(Ticket.class));
    }
    return Optional.empty();
  };

  public static final Function<? super Record, Optional<DashboardWidget>> DASHBOARD_WIDGET_MAPPER = r -> {
    Long widgetId = r.get(DASHBOARD_WIDGET.WIDGET_ID);
    if (widgetId == null) {
      return empty();
    }
    DashboardWidget dashboardWidget = new DashboardWidget();
    dashboardWidget.setId(new DashboardWidgetId(r.get(DASHBOARD.ID), widgetId));
    dashboardWidget.setPositionX(r.get(DASHBOARD_WIDGET.WIDGET_POSITION_X));
    dashboardWidget.setPositionY(r.get(DASHBOARD_WIDGET.WIDGET_POSITION_Y));
    dashboardWidget.setHeight(r.get(DASHBOARD_WIDGET.WIDGET_HEIGHT));
    dashboardWidget.setWidth(r.get(DASHBOARD_WIDGET.WIDGET_WIDTH));
    dashboardWidget.setCreatedOn(r.get(DASHBOARD_WIDGET.IS_CREATED_ON));
    dashboardWidget.setWidgetOwner(r.get(DASHBOARD_WIDGET.WIDGET_OWNER));
    dashboardWidget.setWidgetName(r.get(DASHBOARD_WIDGET.WIDGET_NAME));
    dashboardWidget.setWidgetType(r.get(DASHBOARD_WIDGET.WIDGET_TYPE));
    final Widget widget = new Widget();
    WIDGET_OPTIONS_MAPPER.accept(widget, r);
    dashboardWidget.setWidget(widget);
    return Optional.of(dashboardWidget);
  };

  public static final RecordMapper<? super Record, IntegrationType> INTEGRATION_TYPE_MAPPER = r -> {
    IntegrationType integrationType = new IntegrationType();
    integrationType.setId(r.get(INTEGRATION_TYPE.ID, Long.class));
    integrationType.setEnabled(r.get(INTEGRATION_TYPE.ENABLED));
    integrationType.setCreationDate(r.get(INTEGRATION_TYPE.CREATION_DATE));
    ofNullable(r.get(INTEGRATION_TYPE.AUTH_FLOW)).ifPresent(af -> {
      integrationType.setAuthFlow(IntegrationAuthFlowEnum.findByName(af.getLiteral())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE)));
    });

    integrationType.setName(r.get(INTEGRATION_TYPE.NAME));
    integrationType.setIntegrationGroup(
        IntegrationGroupEnum.findByName(r.get(INTEGRATION_TYPE.GROUP_TYPE).getLiteral())
            .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE)));

    String detailsJson = r.get(INTEGRATION_TYPE.DETAILS, String.class);
    ofNullable(detailsJson).ifPresent(s -> {
      try {
        IntegrationTypeDetails details = objectMapper.readValue(s, IntegrationTypeDetails.class);
        integrationType.setDetails(details);
      } catch (IOException e) {
        throw new ReportPortalException("Error during parsing integration type details");
      }
    });

    return integrationType;
  };

  public static final BiConsumer<? super Integration, ? super Record> INTEGRATION_PARAMS_MAPPER = (i, r) -> {
    String paramsJson = r.get(INTEGRATION.PARAMS, String.class);
    ofNullable(paramsJson).ifPresent(s -> {
      try {
        IntegrationParams params = objectMapper.readValue(s, IntegrationParams.class);
        i.setParams(params);
      } catch (IOException e) {
        throw new ReportPortalException("Error during parsing integration params");
      }
    });
  };

  public static final RecordMapper<? super Record, Integration> GLOBAL_INTEGRATION_RECORD_MAPPER = r -> {

    Integration integration = new Integration();
    integration.setId(r.get(INTEGRATION.ID, Long.class));
    integration.setName(r.get(INTEGRATION.NAME));
    integration.setType(INTEGRATION_TYPE_MAPPER.map(r));
    integration.setCreator(r.get(INTEGRATION.CREATOR));
    integration.setCreationDate(r.get(INTEGRATION.CREATION_DATE));
    integration.setEnabled(r.get(INTEGRATION.ENABLED));
    INTEGRATION_PARAMS_MAPPER.accept(integration, r);

    return integration;
  };

  public static final RecordMapper<? super Record, Integration> PROJECT_INTEGRATION_RECORD_MAPPER = r -> {

    Integration integration = GLOBAL_INTEGRATION_RECORD_MAPPER.map(r);

    Project project = new Project();
    project.setId(r.get(INTEGRATION.PROJECT_ID));

    integration.setProject(project);

    return integration;
  };

  /**
   * Maps record into {@link TmsTestCase} object
   */
  public static final RecordMapper<? super Record, TmsTestCase> TMS_TEST_CASE_MAPPER = r -> {
    TmsTestCase testCase = new TmsTestCase();
    testCase.setId(r.get(TMS_TEST_CASE.ID));
    testCase.setName(r.get(TMS_TEST_CASE.NAME));
    testCase.setDescription(r.get(TMS_TEST_CASE.DESCRIPTION));
    testCase.setPriority(r.get(TMS_TEST_CASE.PRIORITY));

    ofNullable(r.get(TMS_TEST_CASE.TEST_FOLDER_ID)).ifPresent(folderId -> {
      TmsTestFolder testFolder = new TmsTestFolder();
      testFolder.setId(folderId);

      ofNullable(r.field("project_id")).flatMap(f -> ofNullable(r.get(f, Long.class)))
          .ifPresent(projectId -> {
            Project project = new Project();
            project.setId(projectId);
            testFolder.setProject(project);
          });

      testCase.setTestFolder(testFolder);
    });

    return testCase;
  };

  /**
   * Maps record into {@link TmsTestPlan} object
   */
  public static final RecordMapper<? super Record, TmsTestPlan> TMS_TEST_PLAN_MAPPER = r -> {
    TmsTestPlan testPlan = new TmsTestPlan();
    testPlan.setId(r.get(TMS_TEST_PLAN.ID));
    testPlan.setName(r.get(TMS_TEST_PLAN.NAME));
    testPlan.setDescription(r.get(TMS_TEST_PLAN.DESCRIPTION));

    ofNullable(r.get(TMS_TEST_PLAN.PROJECT_ID)).ifPresent(projectId -> {
      Project project = new Project();
      project.setId(projectId);
      testPlan.setProject(project);
    });

    return testPlan;
  };

  /**
   * Maps record into {@link TmsTestFolder} object
   */
  public static final RecordMapper<? super Record, TmsTestFolder> TMS_TEST_FOLDER_MAPPER = r -> {
    TmsTestFolder testFolder = new TmsTestFolder();
    testFolder.setId(r.get(TMS_TEST_FOLDER.ID));
    testFolder.setName(r.get(TMS_TEST_FOLDER.NAME));
    testFolder.setDescription(r.get(TMS_TEST_FOLDER.DESCRIPTION));
    testFolder.setIndex(r.get(TMS_TEST_FOLDER.INDEX));

    ofNullable(r.get(TMS_TEST_FOLDER.PROJECT_ID)).ifPresent(projectId -> {
      Project project = new Project();
      project.setId(projectId);
      testFolder.setProject(project);
    });

    ofNullable(r.get(TMS_TEST_FOLDER.PARENT_ID)).ifPresent(parentId -> {
      TmsTestFolder parentTestFolder = new TmsTestFolder();
      parentTestFolder.setId(parentId);
      testFolder.setParentTestFolder(parentTestFolder);
    });

    return testFolder;
  };

  /**
   * Maps record into {@link TmsAttribute} object
   */
  public static final RecordMapper<? super Record, TmsAttribute> TMS_ATTRIBUTE_MAPPER = r -> {
    TmsAttribute attribute = new TmsAttribute();
    attribute.setId(r.get(TMS_ATTRIBUTE.ID));
    attribute.setKey(r.get(TMS_ATTRIBUTE.KEY));
    attribute.setValue(r.get(TMS_ATTRIBUTE.VALUE));
    return attribute;
  };

  /**
   * Maps record into {@link TmsTestCaseExecution} object
   */
  public static final RecordMapper<? super Record, TmsTestCaseExecution> TMS_TEST_CASE_EXECUTION_MAPPER = r -> {
    TmsTestCaseExecution execution = new TmsTestCaseExecution();
    execution.setId(r.get(TMS_TEST_CASE_EXECUTION.ID));
    execution.setTestCaseId(r.get(TMS_TEST_CASE_EXECUTION.TEST_CASE_ID));
    execution.setLaunchId(r.get(TMS_TEST_CASE_EXECUTION.LAUNCH_ID));
    execution.setTestCaseVersionId(r.get(TMS_TEST_CASE_EXECUTION.TEST_CASE_VERSION_ID));
    execution.setPriority(r.get(TMS_TEST_CASE_EXECUTION.PRIORITY, String.class));
    execution.setTestCaseSnapshot(r.get(TMS_TEST_CASE_EXECUTION.TEST_CASE_SNAPSHOT, String.class));

    //Create TestItem with id, if test_item_id exists
    ofNullable(r.get(TMS_TEST_CASE_EXECUTION.TEST_ITEM_ID)).ifPresent(testItemId -> {
      TestItem testItem = new TestItem();
      testItem.setItemId(testItemId);

      // If there is test_item from JOIN - fill that
      ofNullable(r.field(TEST_ITEM.START_TIME)).flatMap(f -> ofNullable(r.get(f, Instant.class)))
          .ifPresent(testItem::setStartTime);
      ofNullable(r.field(TEST_ITEM.NAME)).flatMap(f -> ofNullable(r.get(f, String.class)))
          .ifPresent(testItem::setName);
      ofNullable(r.field(TEST_ITEM.PARENT_ID)).flatMap(f -> ofNullable(r.get(f, Long.class)))
          .ifPresent(testItem::setParentId);

      // Get TestItemResults
      ofNullable(r.field(TEST_ITEM_RESULTS.RESULT_ID))
          .flatMap(f -> ofNullable(r.get(f, Long.class)))
          .ifPresent(resultId -> {
            TestItemResults results = new TestItemResults();
            results.setItemId(resultId);
            ofNullable(r.field(TEST_ITEM_RESULTS.STATUS)).flatMap(f -> ofNullable(r.get(f)))
                .ifPresent(status -> results.setStatus(StatusEnum.valueOf(status.getLiteral())));
            ofNullable(r.field(TEST_ITEM_RESULTS.END_TIME)).flatMap(f -> ofNullable(r.get(f, Instant.class)))
                .ifPresent(results::setEndTime);
            testItem.setItemResults(results);
          });

      execution.setTestItem(testItem);
    });

    ofNullable(r.field(TMS_TEST_CASE_EXECUTION_COMMENT.ID))
        .flatMap(f -> ofNullable(r.get(f, Long.class)))
        .ifPresent(commentId -> {
          TmsTestCaseExecutionComment comment = new TmsTestCaseExecutionComment();
          comment.setId(commentId);
          ofNullable(r.field(TMS_TEST_CASE_EXECUTION_COMMENT.COMMENT))
              .flatMap(f -> ofNullable(r.get(f, String.class)))
              .ifPresent(comment::setComment);
          execution.setExecutionComment(comment);
        });

    return execution;
  };

  /**
   * Maps record into {@link TmsTestFolderTestItem} object
   */
  public static final RecordMapper<? super Record, TmsTestFolderTestItem> TMS_TEST_FOLDER_TEST_ITEM_MAPPER = r -> {
    TmsTestFolderTestItem item = new TmsTestFolderTestItem();
    item.setId(r.get(TMS_TEST_FOLDER_TEST_ITEM.ID));
    item.setName(r.get(TMS_TEST_FOLDER_TEST_ITEM.NAME));
    item.setDescription(r.get(TMS_TEST_FOLDER_TEST_ITEM.DESCRIPTION));
    item.setTestFolderId(r.get(TMS_TEST_FOLDER_TEST_ITEM.TEST_FOLDER_ID));
    item.setLaunchId(r.get(TMS_TEST_FOLDER_TEST_ITEM.LAUNCH_ID));

    ofNullable(r.get(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID)).ifPresent(itemId -> {
        TestItem testItem = new TestItem();
        testItem.setItemId(itemId);
        item.setTestItem(testItem);
    });

    return item;
  };
}