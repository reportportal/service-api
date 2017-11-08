/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.project.settings.impl;

import com.epam.ta.reportportal.core.project.settings.ICreateProjectSettingsHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.events.DefectTypeCreatedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.WidgetDataTypes.LAUNCHES_TABLE;
import static com.epam.ta.reportportal.core.widget.content.WidgetDataTypes.PIE_CHART;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;

/**
 * Basic realization of
 * {@link com.epam.ta.reportportal.core.project.settings.ICreateProjectSettingsHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateProjectSettingsHandler implements ICreateProjectSettingsHandler {

	private static final Map<String, String> PREFIX = ImmutableMap.<String, String>builder().put(AUTOMATION_BUG.getValue(), "ab_")
			.put(PRODUCT_BUG.getValue(), "pb_")
			.put(SYSTEM_ISSUE.getValue(), "si_")
			.put(NO_DEFECT.getValue(), "nd_")
			.put(TO_INVESTIGATE.getValue(), "ti_")
			.build();

	private final ProjectRepository projectRepo;

	private final WidgetRepository widgetRepository;

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public CreateProjectSettingsHandler(ProjectRepository projectRepository, WidgetRepository widgetRepository,
			ApplicationEventPublisher eventPublisher) {
		this.projectRepo = projectRepository;
		this.widgetRepository = widgetRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public EntryCreatedRS createProjectIssueSubType(String projectName, String username, CreateIssueSubTypeRQ rq) {
		Project project = projectRepo.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		expect(TO_INVESTIGATE.getValue().equalsIgnoreCase(rq.getTypeRef()), equalTo(false)).verify(BAD_REQUEST_ERROR,
				"Impossible to create sub-type for 'To Investigate' type."
		);
		expect(NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(rq.getTypeRef()), equalTo(false)).verify(BAD_REQUEST_ERROR,
				"Impossible to create sub-type for 'Not Issue' type."
		);

		/* Check if global issue type reference is valid */
		TestItemIssueType expectedType = fromValue(rq.getTypeRef());
		expect(expectedType, notNull()).verify(BAD_REQUEST_ERROR, rq.getTypeRef());

		/* Settings should be created by external DB script */

		/* Already existing types */
		Map<TestItemIssueType, List<StatisticSubType>> subTypes = project.getConfiguration().getSubTypes();

		expect(subTypes.get(expectedType).size() < ValidationConstraints.MAX_ISSUE_SUBTYPES, equalTo(true)).verify(BAD_REQUEST_ERROR,
				"Sub Issues count is bound of size limit"
		);

		/* Request fields should be validated till here already */
		String newID = PREFIX.get(expectedType.getValue()) + shortUUID();
		StatisticSubType subType = new StatisticSubType(newID, expectedType.getValue(), rq.getLongName(), rq.getShortName().toUpperCase(),
				rq.getColor()
		);
		subTypes.get(expectedType).add(subType);
		project.getConfiguration().setSubTypes(subTypes);

		/* May be change on direct Update operation */
		try {
			projectRepo.save(project);
			widgetRepository.findByProject(projectName)
					.stream()
					.filter(it -> it.getContentOptions().getType().equals(PIE_CHART.getType()) || it.getContentOptions()
							.getType()
							.equals(LAUNCHES_TABLE.getType()))
					.filter(it -> it.getContentOptions()
							.getContentFields()
							.stream()
							.anyMatch(s -> s.contains(subType.getTypeRef().toLowerCase())))
					.forEach(it -> widgetRepository.addContentField(it.getId(),
							"statistics$defects$" + subType.getTypeRef().toLowerCase() + "$" + subType.getLocator()
					));
		} catch (Exception e) {
			throw new ReportPortalException("Error during creation of custom project sub-type", e);
		}
		eventPublisher.publishEvent(new DefectTypeCreatedEvent(projectName, username, subType));
		return new EntryCreatedRS(newID);
	}

	private static String shortUUID() {
		long l = ByteBuffer.wrap(UUID.randomUUID().toString().getBytes(Charsets.UTF_8)).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}
}
