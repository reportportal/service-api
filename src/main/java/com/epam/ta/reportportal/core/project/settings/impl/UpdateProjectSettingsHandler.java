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
import com.epam.ta.reportportal.core.events.activity.DefectTypeUpdatedEvent;
import com.epam.ta.reportportal.core.project.settings.IUpdateProjectSettingsHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.UpdateOneIssueSubTypeRQ;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class UpdateProjectSettingsHandler implements IUpdateProjectSettingsHandler {

	private ProjectRepository projectRepository;

	private MessageBus messageBus;

	@Autowired
	public UpdateProjectSettingsHandler(ProjectRepository projectRepository, MessageBus messageBus) {
		this.projectRepository = projectRepository;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS updateProjectIssueSubType(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			UpdateIssueSubTypeRQ rq) {
		expect(rq.getIds().size() > 0, equalTo(true)).verify(FORBIDDEN_OPERATION, "Please specify at least one item data for update.");

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		rq.getIds().forEach(r -> validateAndUpdate(r, project.getIssueTypes()));

		projectRepository.save(project);
		rq.getIds().forEach(one -> messageBus.publishActivity(new DefectTypeUpdatedEvent(project.getId(), user.getUserId(), one)));
		return new OperationCompletionRS("Issue sub-type(s) was updated successfully.");
	}

	private void validateAndUpdate(UpdateOneIssueSubTypeRQ one, List<IssueType> settings) {
		/* Check if global issue type reference is valid */
		TestItemIssueGroup expectedType = TestItemIssueGroup.fromValue(one.getTypeRef())
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, one.getTypeRef()));

		IssueType exist = settings.stream()
				.filter(issueType -> issueType.getLocator().equalsIgnoreCase(one.getId()))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, one.getId()));

		expect(exist.getIssueGroup().equals(expectedType), equalTo(true)).verify(FORBIDDEN_OPERATION,
				"You cannot change sub-type references to global type."
		);

		if (Sets.newHashSet(AUTOMATION_BUG.getLocator(),
				PRODUCT_BUG.getLocator(),
				SYSTEM_ISSUE.getLocator(),
				NO_DEFECT.getLocator(),
				TO_INVESTIGATE.getLocator()
		).contains(exist.getLocator())) {
			fail().withError(FORBIDDEN_OPERATION, "You cannot edit predefined global issue types.");
		}

		exist.setLongName(one.getLongName());
		exist.setShortName(one.getShortName());
		exist.setHexColor(one.getColor());
	}
}
