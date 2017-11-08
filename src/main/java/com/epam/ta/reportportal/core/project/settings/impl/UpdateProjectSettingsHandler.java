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

import com.epam.ta.reportportal.core.project.settings.IUpdateProjectSettingsHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.events.DefectTypeUpdatedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.UpdateOneIssueSubTypeRQ;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Initial realization of
 * {@link com.epam.ta.reportportal.core.project.settings.IUpdateProjectSettingsHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateProjectSettingsHandler implements IUpdateProjectSettingsHandler {

	private ProjectRepository projectRepo;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public UpdateProjectSettingsHandler(ProjectRepository projectRepo, ApplicationEventPublisher eventPublisher) {
		this.projectRepo = projectRepo;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public OperationCompletionRS updateProjectIssueSubType(String projectName, String user, UpdateIssueSubTypeRQ rq) {

		expect(rq.getIds().size() > 0, equalTo(true)).verify(FORBIDDEN_OPERATION, "Please specify at least one item data for update.");

		Project project = projectRepo.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		expect(project.getConfiguration(), notNull()).verify(PROJECT_SETTINGS_NOT_FOUND, projectName);

		rq.getIds().stream().forEach(r -> validateAndUpdate(r, project.getConfiguration()));

		try {
			projectRepo.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during update of custom project issue sub-type", e);
		}
		eventPublisher.publishEvent(new DefectTypeUpdatedEvent(projectName, user, rq));
		return new OperationCompletionRS("Issue sub-type(s) was updated successfully.");
	}

	@SuppressWarnings("unused")
	private static <T> Collector<T, ?, T> findOneCollector(String id) {
		return Collectors.collectingAndThen(Collectors.toList(), shouldBeOne -> {
			if (shouldBeOne.size() != 1) {
				throw new ReportPortalException(String.format("No such issue sub-type found for id='%s'", id));
			}
			return shouldBeOne.get(0);
		});
	}

	/**
	 * Validate provided issue sub-type from RQ and update project settings
	 * object if validations are OK
	 *
	 * @param one
	 */
	private void validateAndUpdate(UpdateOneIssueSubTypeRQ one, Project.Configuration settings) {
		/* Check if global issue type reference is valid */
		TestItemIssueType expectedType = fromValue(one.getTypeRef());
		expect(expectedType, notNull()).verify(ISSUE_TYPE_NOT_FOUND, one.getTypeRef());

		StatisticSubType exist = settings.getByLocator(one.getId());
		expect(exist, notNull()).verify(ISSUE_TYPE_NOT_FOUND, one.getId());
		expect(
				exist.getTypeRef().equals(expectedType.getValue()) || exist.getTypeRef().equals(IssueCounter.GROUP_TOTAL),
				equalTo(true)
		).verify(FORBIDDEN_OPERATION, "You cannot change sub-type references to global type.");

		if (Sets.newHashSet(AUTOMATION_BUG.getLocator(), PRODUCT_BUG.getLocator(), SYSTEM_ISSUE.getLocator(), NO_DEFECT.getLocator(),
				TO_INVESTIGATE.getLocator(), IssueCounter.GROUP_TOTAL
		).contains(exist.getLocator())) {

			fail().withError(FORBIDDEN_OPERATION, "You cannot edit predefined global issue types.");
		}
		StatisticSubType type = new StatisticSubType(one.getId(), one.getTypeRef(), one.getLongName(), one.getShortName(), one.getColor());
		settings.setByLocator(type);
	}
}