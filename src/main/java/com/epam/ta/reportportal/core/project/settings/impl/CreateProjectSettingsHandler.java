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
import com.epam.ta.reportportal.core.project.settings.ICreateProjectSettingsHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class CreateProjectSettingsHandler implements ICreateProjectSettingsHandler {

	private ProjectRepository repository;

	@Autowired
	public CreateProjectSettingsHandler(ProjectRepository repository) {
		this.repository = repository;
	}

	@Override
	public EntryCreatedRS createProjectIssueSubType(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			CreateIssueSubTypeRQ rq) {
		Project project = repository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		expect(TO_INVESTIGATE.getValue().equalsIgnoreCase(rq.getTypeRef()), equalTo(false)).verify(BAD_REQUEST_ERROR,
				"Impossible to create sub-type for 'To Investigate' type."
		);
		expect(NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(rq.getTypeRef()), equalTo(false)).verify(BAD_REQUEST_ERROR,
				"Impossible to create sub-type for 'Not Issue' type."
		);

		/* Check if global issue type reference is valid */
		TestItemIssueGroup expectedType = TestItemIssueGroup.fromValue(rq.getTypeRef())
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, rq.getTypeRef()));

		List<IssueType> existingSubTypes = project.getIssueTypes()
				.stream()
				.filter(issueType -> issueType.getIssueGroup().equals(expectedType))
				.collect(Collectors.toList());

		expect(existingSubTypes.size() < ValidationConstraints.MAX_ISSUE_SUBTYPES, equalTo(true)).verify(BAD_REQUEST_ERROR,
				"Sub Issues count is bound of size limit"
		);

		IssueType subType = new IssueType();
		subType.setLongName(rq.getLongName());
		subType.setShortName(rq.getShortName());
		subType.setHexColor(rq.getColor());

		return null;
	}
}
