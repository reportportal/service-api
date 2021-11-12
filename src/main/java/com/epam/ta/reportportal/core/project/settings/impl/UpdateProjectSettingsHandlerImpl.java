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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DefectTypeUpdatedEvent;
import com.epam.ta.reportportal.core.events.activity.PatternUpdatedEvent;
import com.epam.ta.reportportal.core.project.settings.UpdateProjectSettingsHandler;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.PatternTemplateConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.IssueTypeActivityResource;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.UpdateOneIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.pattern.UpdatePatternTemplateRQ;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
import static com.epam.ta.reportportal.ws.converter.converters.IssueTypeConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class UpdateProjectSettingsHandlerImpl implements UpdateProjectSettingsHandler {

	private final ProjectRepository projectRepository;

	private final PatternTemplateRepository patternTemplateRepository;

	private final MessageBus messageBus;

	@Autowired
	public UpdateProjectSettingsHandlerImpl(ProjectRepository projectRepository, PatternTemplateRepository patternTemplateRepository,
			MessageBus messageBus) {
		this.projectRepository = projectRepository;
		this.patternTemplateRepository = patternTemplateRepository;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS updateProjectIssueSubType(String projectName, ReportPortalUser user,
			UpdateIssueSubTypeRQ updateIssueSubTypeRQ) {
		expect(updateIssueSubTypeRQ.getIds().size() > 0, equalTo(true)).verify(FORBIDDEN_OPERATION,
				"Please specify at least one item data for update."
		);

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectName));

		List<IssueTypeActivityResource> issueTypeActivityResources = updateIssueSubTypeRQ.getIds()
				.stream()
				.map(subTypeRQ -> TO_ACTIVITY_RESOURCE.apply(validateAndUpdate(subTypeRQ,
						project.getProjectIssueTypes().stream().map(ProjectIssueType::getIssueType).collect(Collectors.toList())
				)))
				.collect(Collectors.toList());

		projectRepository.save(project);
		issueTypeActivityResources.forEach(it -> messageBus.publishActivity(new DefectTypeUpdatedEvent(it,
				user.getUserId(),
				user.getUsername(),
				project.getId()
		)));
		return new OperationCompletionRS("Issue sub-type(s) was updated successfully.");
	}

	@Override
	public OperationCompletionRS updatePatternTemplate(Long id, String projectName, UpdatePatternTemplateRQ updatePatternTemplateRQ,
			ReportPortalUser user) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		PatternTemplate patternTemplate = patternTemplateRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PATTERN_TEMPLATE_NOT_FOUND_IN_PROJECT, id, project.getId()));

		final String name = StringUtils.trim(updatePatternTemplateRQ.getName());

		if (!patternTemplate.getName().equalsIgnoreCase(name)) {
			BusinessRule.expect(patternTemplateRepository.existsByProjectIdAndNameIgnoreCase(
					project.getId(), name
			), equalTo(false)).verify(ErrorType.RESOURCE_ALREADY_EXISTS, name);
		}

		PatternTemplateActivityResource before = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(patternTemplate);

		patternTemplate.setName(name);
		patternTemplate.setEnabled(updatePatternTemplateRQ.getEnabled());

		PatternTemplateActivityResource after = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(patternTemplate);

		messageBus.publishActivity(new PatternUpdatedEvent(user.getUserId(), user.getUsername(), before, after));

		return new OperationCompletionRS(Suppliers.formattedSupplier("Pattern template with ID = '{}' has been successfully updated", id)
				.get());

	}

	private IssueType validateAndUpdate(UpdateOneIssueSubTypeRQ issueSubTypeRQ, List<IssueType> issueTypes) {
		/* Check if global issue type reference is valid */
		TestItemIssueGroup expectedGroup = TestItemIssueGroup.fromValue(issueSubTypeRQ.getTypeRef())
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, issueSubTypeRQ.getTypeRef()));

		IssueType exist = issueTypes.stream()
				.filter(issueType -> issueType.getLocator().equalsIgnoreCase(issueSubTypeRQ.getLocator()))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ISSUE_TYPE_NOT_FOUND, issueSubTypeRQ.getLocator()));

		expect(exist.getIssueGroup().getTestItemIssueGroup().equals(expectedGroup), equalTo(true)).verify(FORBIDDEN_OPERATION,
				"You cannot change sub-type references to global type."
		);

		expect(exist.getLocator(),
				not(in(Sets.newHashSet(AUTOMATION_BUG.getLocator(),
						PRODUCT_BUG.getLocator(),
						SYSTEM_ISSUE.getLocator(),
						NO_DEFECT.getLocator(),
						TO_INVESTIGATE.getLocator()
				)))
		).verify(FORBIDDEN_OPERATION, "You cannot remove predefined global issue types.");

		ofNullable(issueSubTypeRQ.getLongName()).ifPresent(exist::setLongName);
		ofNullable(issueSubTypeRQ.getShortName()).ifPresent(exist::setShortName);
		ofNullable(issueSubTypeRQ.getColor()).ifPresent(exist::setHexColor);
		return exist;
	}
}
