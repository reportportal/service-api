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

package com.epam.ta.reportportal.core.project.settings.impl;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.AUTOMATION_BUG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NO_DEFECT;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.SYSTEM_ISSUE;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.converter.converters.IssueTypeConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.INCORRECT_REQUEST;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.pattern.service.CreatePatternTemplateHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DefectTypeCreatedEvent;
import com.epam.ta.reportportal.core.events.activity.PatternCreatedEvent;
import com.epam.ta.reportportal.core.project.settings.CreateProjectSettingsHandler;
import com.epam.ta.reportportal.dao.IssueGroupRepository;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.model.project.config.IssueSubTypeCreatedRS;
import com.epam.ta.reportportal.model.project.config.pattern.CreatePatternTemplateRQ;
import com.epam.ta.reportportal.ws.converter.builders.IssueTypeBuilder;
import com.epam.ta.reportportal.ws.converter.converters.PatternTemplateConverter;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.model.ValidationConstraints;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class CreateProjectSettingsHandlerImpl implements CreateProjectSettingsHandler {

  private static final Map<String, String> PREFIX =
      ImmutableMap.<String, String>builder().put(AUTOMATION_BUG.getValue(), "ab_")
          .put(PRODUCT_BUG.getValue(), "pb_").put(SYSTEM_ISSUE.getValue(), "si_")
          .put(NO_DEFECT.getValue(), "nd_").put(TO_INVESTIGATE.getValue(), "ti_").build();

  private final ProjectRepository projectRepository;

  private final WidgetRepository widgetRepository;

  private final IssueGroupRepository issueGroupRepository;

  private final IssueTypeRepository issueTypeRepository;

  private final Map<PatternTemplateType, CreatePatternTemplateHandler> createPatternTemplateMapping;

  private final MessageBus messageBus;

  @Autowired
  public CreateProjectSettingsHandlerImpl(ProjectRepository projectRepository,
      WidgetRepository widgetRepository, IssueGroupRepository issueGroupRepository,
      IssueTypeRepository issueTypeRepository, @Qualifier("createPatternTemplateMapping")
  Map<PatternTemplateType, CreatePatternTemplateHandler> createPatternTemplateMapping,
      MessageBus messageBus) {
    this.projectRepository = projectRepository;
    this.widgetRepository = widgetRepository;
    this.issueGroupRepository = issueGroupRepository;
    this.issueTypeRepository = issueTypeRepository;
    this.createPatternTemplateMapping = createPatternTemplateMapping;
    this.messageBus = messageBus;
  }

  @Override
  public IssueSubTypeCreatedRS createProjectIssueSubType(String projectKey, ReportPortalUser user,
      CreateIssueSubTypeRQ rq) {
    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectKey));

    expect(NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(rq.getTypeRef()), equalTo(false)).verify(
        INCORRECT_REQUEST, "Impossible to create sub-type for 'Not Issue' type.");

    /* Check if global issue type reference is valid */
    TestItemIssueGroup expectedGroup = TestItemIssueGroup.fromValue(rq.getTypeRef())
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, rq.getTypeRef()));

    expect(
        project.getProjectIssueTypes().size() < ValidationConstraints.MAX_ISSUE_TYPES_AND_SUBTYPES,
        equalTo(true)
    ).verify(INCORRECT_REQUEST, "Sub Issues count is bound of size limit");

    String locator = PREFIX.get(expectedGroup.getValue()) + shortUUID();
    IssueType subType = new IssueTypeBuilder().addLocator(locator)
        .addIssueGroup(issueGroupRepository.findByTestItemIssueGroup(expectedGroup))
        .addLongName(rq.getLongName()).addShortName(rq.getShortName()).addHexColor(rq.getColor())
        .get();

    ProjectIssueType projectIssueType = new ProjectIssueType();
    projectIssueType.setIssueType(subType);
    projectIssueType.setProject(project);

    project.getProjectIssueTypes().add(projectIssueType);

    issueTypeRepository.save(subType);
    projectRepository.save(project);

    updateWidgets(project, subType);

    messageBus.publishActivity(
        new DefectTypeCreatedEvent(TO_ACTIVITY_RESOURCE.apply(subType), user.getUserId(),
            user.getUsername(), project.getId(), project.getOrganizationId()
        ));
    return new IssueSubTypeCreatedRS(subType.getId(), subType.getLocator());
  }

  /**
   * Update {@link Widget#getContentFields()} of the widgets that support issue type updates
   * ({@link WidgetType#isSupportMultilevelStructure()}) and have content fields for the same
   * {@link IssueGroup#getTestItemIssueGroup()} as provided issueType
   *
   * @param project   {@link Project}
   * @param issueType {@link IssueType}
   */
  private void updateWidgets(Project project, IssueType issueType) {
    String issueGroupContentField =
        "statistics$defects$" + issueType.getIssueGroup().getTestItemIssueGroup().getValue()
            .toLowerCase() + "$";
    widgetRepository.findAllByProjectIdAndWidgetTypeInAndContentFieldContaining(project.getId(),
        Arrays.stream(WidgetType.values()).filter(WidgetType::isIssueTypeUpdateSupported)
            .map(WidgetType::getType).collect(Collectors.toList()), issueGroupContentField
    ).forEach(widget -> {
      widget.getContentFields().add(issueGroupContentField + issueType.getLocator());
      widgetRepository.save(widget);
    });
  }

  @Override
  public EntryCreatedRS createPatternTemplate(String projectKey,
      CreatePatternTemplateRQ createPatternTemplateRQ, ReportPortalUser user) {

    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    PatternTemplate patternTemplate = createPatternTemplateMapping.get(
        PatternTemplateType.fromString(createPatternTemplateRQ.getType()).orElseThrow(
            () -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
                Suppliers.formattedSupplier("Unknown pattern template type - '{}'",
                    createPatternTemplateRQ.getType()
                ).get()
            ))).createPatternTemplate(project.getId(), createPatternTemplateRQ);

    messageBus.publishActivity(new PatternCreatedEvent(user.getUserId(), user.getUsername(),
        PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(patternTemplate), project.getOrganizationId()
    ));
    return new EntryCreatedRS(patternTemplate.getId());
  }

  private static String shortUUID() {
    long l = ByteBuffer.wrap(UUID.randomUUID().toString().getBytes(Charsets.UTF_8)).getLong();
    return Long.toString(l, Character.MAX_RADIX);
  }
}
