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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DefectTypeDeletedEvent;
import com.epam.ta.reportportal.core.events.activity.PatternDeletedEvent;
import com.epam.ta.reportportal.core.project.settings.DeleteProjectSettingsHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.PatternTemplateConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.in;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
import static com.epam.ta.reportportal.entity.widget.WidgetType.LAUNCHES_TABLE;
import static com.epam.ta.reportportal.entity.widget.WidgetType.STATISTIC_TREND;
import static com.epam.ta.reportportal.ws.converter.converters.IssueTypeConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class DeleteProjectSettingsHandlerImpl implements DeleteProjectSettingsHandler {

	private final ProjectRepository projectRepository;

	private final StatisticsFieldRepository statisticsFieldRepository;

	private final WidgetRepository widgetRepository;

	private final MessageBus messageBus;

	private final IssueTypeRepository issueTypeRepository;

	private final IssueEntityRepository issueEntityRepository;

	private final PatternTemplateRepository patternTemplateRepository;

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public DeleteProjectSettingsHandlerImpl(ProjectRepository projectRepository, StatisticsFieldRepository statisticsFieldRepository,
			WidgetRepository widgetRepository, MessageBus messageBus, IssueTypeRepository issueTypeRepository,
			IssueEntityRepository issueEntityRepository, PatternTemplateRepository patternTemplateRepository,
			ApplicationEventPublisher eventPublisher) {
		this.projectRepository = projectRepository;
		this.statisticsFieldRepository = statisticsFieldRepository;
		this.widgetRepository = widgetRepository;
		this.messageBus = messageBus;
		this.issueTypeRepository = issueTypeRepository;
		this.issueEntityRepository = issueEntityRepository;
		this.patternTemplateRepository = patternTemplateRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public OperationCompletionRS deleteProjectIssueSubType(String projectName, ReportPortalUser user, Long id) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectName));

		ProjectIssueType type = project.getProjectIssueTypes()
				.stream()
				.filter(projectIssueType -> projectIssueType.getIssueType().getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, id));

		expect(type.getIssueType().getLocator(), not(in(Sets.newHashSet(AUTOMATION_BUG.getLocator(),
				PRODUCT_BUG.getLocator(),
				SYSTEM_ISSUE.getLocator(),
				NO_DEFECT.getLocator(),
				TO_INVESTIGATE.getLocator()
		)))).verify(FORBIDDEN_OPERATION, "You cannot remove predefined global issue types.");

		String issueField =
				"statistics$defects$" + TestItemIssueGroup.fromValue(type.getIssueType().getIssueGroup().getTestItemIssueGroup().getValue())
						.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, type.getIssueType().getIssueGroup()))
						.getValue()
						.toLowerCase() + "$" + type.getIssueType().getLocator();
		statisticsFieldRepository.deleteByName(issueField);

		IssueType defaultGroupIssueType = issueTypeRepository.findByLocator(type.getIssueType()
				.getIssueGroup()
				.getTestItemIssueGroup()
				.getLocator()).orElseThrow(() -> new ReportPortalException(ErrorType.ISSUE_TYPE_NOT_FOUND, type.getIssueType()));
		List<IssueEntity> allByIssueTypeId = issueEntityRepository.findAllByIssueTypeId(id);
		allByIssueTypeId.forEach(issueEntity -> issueEntity.setIssueType(defaultGroupIssueType));

		project.getProjectIssueTypes().remove(type);
		projectRepository.save(project);

		issueTypeRepository.delete(type.getIssueType());

		widgetRepository.findAllByProjectId(project.getId())
				.stream()
				.filter(widget -> LAUNCHES_TABLE.getType().equals(widget.getWidgetType()) || STATISTIC_TREND.getType()
						.equals(widget.getWidgetType()))
				.forEach(widget -> widget.getContentFields()
						.remove("statistics$defects$" + type.getIssueType().getIssueGroup().getTestItemIssueGroup().getValue().toLowerCase()
								+ "$" + type.getIssueType().getLocator()));

		DefectTypeDeletedEvent defectTypeDeletedEvent = new DefectTypeDeletedEvent(TO_ACTIVITY_RESOURCE.apply(type.getIssueType()),
				user.getUserId(),
				user.getUsername(),
				project.getId()
		);
		messageBus.publishActivity(defectTypeDeletedEvent);
		eventPublisher.publishEvent(defectTypeDeletedEvent);
		return new OperationCompletionRS("Issue sub-type delete operation completed successfully.");
	}

	@Override
	public OperationCompletionRS deletePatternTemplate(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Long id) {
		PatternTemplate patternTemplate = patternTemplateRepository.findByIdAndProjectId(id, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PATTERN_TEMPLATE_NOT_FOUND_IN_PROJECT,
						id,
						projectDetails.getProjectName()
				));
		PatternTemplateActivityResource before = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(patternTemplate);

		patternTemplateRepository.deleteById(patternTemplate.getId());

		messageBus.publishActivity(new PatternDeletedEvent(user.getUserId(), user.getUsername(), before));
		return new OperationCompletionRS(Suppliers.formattedSupplier("Pattern template with id = '{}' has been successfully removed.", id)
				.get());
	}
}
