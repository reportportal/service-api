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
import com.epam.ta.reportportal.core.project.settings.IDeleteProjectSettingsHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class DeleteProjectSettingsHandler implements IDeleteProjectSettingsHandler {

	@Autowired
	private ProjectRepository projectRepository;

	@Override
	public OperationCompletionRS deleteProjectIssueSubType(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			String id) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		IssueType type = project.getIssueTypes()
				.stream()
				.filter(issueType -> issueType.getLocator().equalsIgnoreCase(id))
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

		project.setIssueTypes(project.getIssueTypes()
				.stream()
				.filter(issueType -> issueType.getLocator().equalsIgnoreCase(id))
				.collect(toList()));



		return null;
	}
}
