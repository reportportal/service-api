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

package com.epam.reportportal.base.core.project.settings.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.in;
import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.not;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.AUTOMATION_BUG;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.NO_DEFECT;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.SYSTEM_ISSUE;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ISSUE_TYPE_NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.reportportal.base.ws.converter.converters.IssueTypeConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.events.domain.DefectTypeUpdatedEvent;
import com.epam.reportportal.base.core.events.domain.PatternUpdatedEvent;
import com.epam.reportportal.base.core.project.settings.UpdateProjectSettingsHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.PatternTemplateRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.pattern.PatternTemplate;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectIssueType;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.activity.IssueTypeActivityResource;
import com.epam.reportportal.base.model.activity.PatternTemplateActivityResource;
import com.epam.reportportal.base.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.reportportal.base.model.project.config.UpdateOneIssueSubTypeRQ;
import com.epam.reportportal.base.model.project.config.pattern.UpdatePatternTemplateRQ;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.base.ws.converter.converters.PatternTemplateConverter;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateProjectSettingsHandlerImpl implements UpdateProjectSettingsHandler {

  private final ProjectRepository projectRepository;

  private final PatternTemplateRepository patternTemplateRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public OperationCompletionRS updateProjectIssueSubType(String projectKey, ReportPortalUser user,
      UpdateIssueSubTypeRQ updateIssueSubTypeRQ) {
    expect(!updateIssueSubTypeRQ.getIds().isEmpty(), equalTo(true)).verify(FORBIDDEN_OPERATION,
        "Please specify at least one item data for update."
    );

    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectKey));

    List<IssueTypeActivityResource> issueTypeActivityResources =
        updateIssueSubTypeRQ.getIds().stream().map(subTypeRQ -> TO_ACTIVITY_RESOURCE.apply(
            validateAndUpdate(subTypeRQ,
                project.getProjectIssueTypes().stream().map(ProjectIssueType::getIssueType)
                    .collect(Collectors.toList())
            ))).collect(Collectors.toList());

    projectRepository.save(project);
    issueTypeActivityResources.forEach(it -> eventPublisher.publishEvent(
        new DefectTypeUpdatedEvent(it, user.getUserId(), user.getUsername(), project.getId(),
            project.getOrganizationId())));
    return new OperationCompletionRS("Issue sub-type(s) was updated successfully.");
  }

  @Override
  public OperationCompletionRS updatePatternTemplate(Long id, String projectKey,
      UpdatePatternTemplateRQ updatePatternTemplateRQ, ReportPortalUser user) {

    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    PatternTemplate patternTemplate = patternTemplateRepository.findByIdAndProjectId(id,
        project.getId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.PATTERN_TEMPLATE_NOT_FOUND_IN_PROJECT, id,
            project.getId()
        ));

    final String name = StringUtils.trim(updatePatternTemplateRQ.getName());

    if (!patternTemplate.getName().equalsIgnoreCase(name)) {
      BusinessRule.expect(
          patternTemplateRepository.existsByProjectIdAndNameIgnoreCase(project.getId(), name),
          equalTo(false)
      ).verify(ErrorType.RESOURCE_ALREADY_EXISTS, name);
    }

    PatternTemplateActivityResource before =
        PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(patternTemplate);

    patternTemplate.setName(name);
    patternTemplate.setEnabled(updatePatternTemplateRQ.getEnabled());

    PatternTemplateActivityResource after =
        PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(patternTemplate);

    eventPublisher.publishEvent(
        new PatternUpdatedEvent(user.getUserId(), user.getUsername(), before, after,
            project.getOrganizationId()));

    return new OperationCompletionRS(
        Suppliers.formattedSupplier("Pattern template with ID = '{}' has been successfully updated",
            id
        ).get());

  }

  private IssueType validateAndUpdate(UpdateOneIssueSubTypeRQ issueSubTypeRQ,
      List<IssueType> issueTypes) {
    /* Check if global issue type reference is valid */
    TestItemIssueGroup expectedGroup = TestItemIssueGroup.fromValue(issueSubTypeRQ.getTypeRef())
        .orElseThrow(
            () -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, issueSubTypeRQ.getTypeRef()));

    IssueType exist = issueTypes.stream()
        .filter(issueType -> issueType.getLocator().equalsIgnoreCase(issueSubTypeRQ.getLocator()))
        .findFirst().orElseThrow(
            () -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, issueSubTypeRQ.getLocator()));

    expect(exist.getIssueGroup().getTestItemIssueGroup().equals(expectedGroup),
        equalTo(true)
    ).verify(FORBIDDEN_OPERATION, "You cannot change sub-type references to global type.");

    expect(exist.getLocator(), not(in(
        Sets.newHashSet(AUTOMATION_BUG.getLocator(), PRODUCT_BUG.getLocator(),
            SYSTEM_ISSUE.getLocator(), NO_DEFECT.getLocator(), TO_INVESTIGATE.getLocator()
        )))).verify(FORBIDDEN_OPERATION, "You cannot remove predefined global issue types.");

    ofNullable(issueSubTypeRQ.getLongName()).ifPresent(exist::setLongName);
    ofNullable(issueSubTypeRQ.getShortName()).ifPresent(exist::setShortName);
    ofNullable(issueSubTypeRQ.getColor()).ifPresent(exist::setHexColor);
    return exist;
  }
}
