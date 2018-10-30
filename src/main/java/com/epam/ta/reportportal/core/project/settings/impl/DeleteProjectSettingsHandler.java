/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.project.settings.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DefectTypeDeletedEvent;
import com.epam.ta.reportportal.core.project.settings.IDeleteProjectSettingsHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
import static com.epam.ta.reportportal.entity.widget.WidgetType.LAUNCHES_TABLE;
import static com.epam.ta.reportportal.entity.widget.WidgetType.STATISTIC_TREND;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class DeleteProjectSettingsHandler implements IDeleteProjectSettingsHandler {

	private ProjectRepository projectRepository;

	private StatisticsFieldRepository statisticsFieldRepository;

	private WidgetRepository widgetRepository;

	private MessageBus messageBus;

	private IssueTypeRepository issueTypeRepository;

	private IssueEntityRepository issueEntityRepository;

	@Autowired
	public DeleteProjectSettingsHandler(ProjectRepository projectRepository, StatisticsFieldRepository statisticsFieldRepository,
			WidgetRepository widgetRepository, MessageBus messageBus, IssueTypeRepository issueTypeRepository,
			IssueEntityRepository issueEntityRepository) {
		this.projectRepository = projectRepository;
		this.statisticsFieldRepository = statisticsFieldRepository;
		this.widgetRepository = widgetRepository;
		this.messageBus = messageBus;
		this.issueTypeRepository = issueTypeRepository;
		this.issueEntityRepository = issueEntityRepository;
	}

	@Override
	public OperationCompletionRS deleteProjectIssueSubType(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Long id) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		IssueType type = project.getIssueTypes()
				.stream().filter(issueType -> issueType.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, id));

		if (Sets.newHashSet(
				AUTOMATION_BUG.getLocator(),
				PRODUCT_BUG.getLocator(),
				SYSTEM_ISSUE.getLocator(),
				NO_DEFECT.getLocator(),
				TO_INVESTIGATE.getLocator()
		).contains(type.getLocator())) {
			fail().withError(FORBIDDEN_OPERATION, "You cannot remove predefined global issue types.");
		}

		String issueField = "statistics$defects$" + TestItemIssueGroup.fromValue(type.getIssueGroup().getTestItemIssueGroup().getValue())
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, type.getIssueGroup()))
				.getValue()
				.toLowerCase() + "$" + type.getLocator();
		statisticsFieldRepository.deleteByName(issueField);

		IssueType defaultGroupIssueType = issueTypeRepository.findByLocator(type.getIssueGroup().getTestItemIssueGroup().getLocator());
		List<IssueEntity> allByIssueTypeId = issueEntityRepository.findAllByIssueTypeId(id);
		allByIssueTypeId.forEach(issueEntity -> issueEntity.setIssueType(defaultGroupIssueType));

		project.getIssueTypes().remove(type);
		projectRepository.save(project);

		widgetRepository.findAllByProjectId(projectDetails.getProjectId()).stream().filter(widget -> {
			String widgetType = widget.getWidgetType();
			return widgetType.equals(LAUNCHES_TABLE.getType()) || widgetType.equals(STATISTIC_TREND.getType());
		}).forEach(widget -> {
			widget.getContentFields()
					.remove("statistics$defects$" + type.getIssueGroup().getTestItemIssueGroup().getValue().toLowerCase() + "$"
							+ type.getLocator());
			widgetRepository.save(widget);
		});
		issueTypeRepository.delete(type);

		messageBus.publishActivity(new DefectTypeDeletedEvent(type, project.getId(), user.getUserId()));
		return new OperationCompletionRS("Issue sub-type delete operation completed successfully.");
	}
}
