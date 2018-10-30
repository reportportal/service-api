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
import com.epam.ta.reportportal.core.events.activity.DefectTypeCreatedEvent;
import com.epam.ta.reportportal.core.project.settings.ICreateProjectSettingsHandler;
import com.epam.ta.reportportal.dao.IssueGroupRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.IssueTypeBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class CreateProjectSettingsHandler implements ICreateProjectSettingsHandler {

	private static final Map<String, String> PREFIX = ImmutableMap.<String, String>builder().put(AUTOMATION_BUG.getValue(), "ab_")
			.put(PRODUCT_BUG.getValue(), "pb_")
			.put(SYSTEM_ISSUE.getValue(), "si_")
			.put(NO_DEFECT.getValue(), "nd_")
			.put(TO_INVESTIGATE.getValue(), "ti_")
			.build();

	private ProjectRepository projectRepository;

	private WidgetRepository widgetRepository;

	private IssueGroupRepository issueGroupRepository;

	private MessageBus messageBus;

	@Autowired
	public CreateProjectSettingsHandler(ProjectRepository projectRepository, WidgetRepository widgetRepository,
			IssueGroupRepository issueGroupRepository, MessageBus messageBus) {
		this.projectRepository = projectRepository;
		this.widgetRepository = widgetRepository;
		this.issueGroupRepository = issueGroupRepository;
		this.messageBus = messageBus;
	}

	@Override
	public EntryCreatedRS createProjectIssueSubType(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			CreateIssueSubTypeRQ rq) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		expect(TO_INVESTIGATE.getValue().equalsIgnoreCase(rq.getTypeRef()), equalTo(false)).verify(BAD_REQUEST_ERROR,
				"Impossible to create sub-type for 'To Investigate' type."
		);
		expect(NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(rq.getTypeRef()), equalTo(false)).verify(BAD_REQUEST_ERROR,
				"Impossible to create sub-type for 'Not Issue' type."
		);

		/* Check if global issue type reference is valid */
		TestItemIssueGroup expectedGroup = TestItemIssueGroup.fromValue(rq.getTypeRef())
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, rq.getTypeRef()));

		List<IssueType> existingSubTypes = project.getIssueTypes()
				.stream()
				.filter(issueType -> issueType.getIssueGroup().getTestItemIssueGroup().equals(expectedGroup))
				.collect(Collectors.toList());

		expect(existingSubTypes.size() < ValidationConstraints.MAX_ISSUE_SUBTYPES, equalTo(true)).verify(BAD_REQUEST_ERROR,
				"Sub Issues count is bound of size limit"
		);

		String locator = PREFIX.get(expectedGroup.getValue()) + shortUUID();
		IssueType subType = new IssueTypeBuilder().addLocator(locator)
				.addIssueGroup(issueGroupRepository.findByTestItemIssueGroup(expectedGroup))
				.addLongName(rq.getLongName())
				.addShortName(rq.getShortName())
				.addHexColor(rq.getColor())
				.addProject(project)
				.get();

		project.getIssueTypes().add(subType);
		projectRepository.save(project);
		widgetRepository.findAllByProjectId(project.getId())
				.stream()
				.filter(widget -> widget.getContentFields()
						.stream()
						.anyMatch(s -> s.contains(subType.getIssueGroup().getTestItemIssueGroup().getValue().toLowerCase())))
				.forEach(widget -> {
					widget.getContentFields()
							.add("statistics$defects$" + subType.getIssueGroup().getTestItemIssueGroup().getValue().toLowerCase() + "$"
									+ subType.getLocator());
					widgetRepository.save(widget);
				});

		Long id = project.getIssueTypes()
				.stream()
				.filter(issueType -> issueType.getLocator().equalsIgnoreCase(locator))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, locator))
				.getId();
		subType.setId(id);

		messageBus.publishActivity(new DefectTypeCreatedEvent(subType, project.getId(), user.getUserId()));
		return new EntryCreatedRS(id);
	}

	private static String shortUUID() {
		long l = ByteBuffer.wrap(UUID.randomUUID().toString().getBytes(Charsets.UTF_8)).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}
}
