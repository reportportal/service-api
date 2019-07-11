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

package com.epam.ta.reportportal.core.project.settings;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.pattern.UpdatePatternTemplateRQ;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface UpdateProjectSettingsHandler {

	/**
	 * Update issue sub-type for specified project
	 *
	 * @param projectName Project name
	 * @param rq          Update rq
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS updateProjectIssueSubType(String projectName, ReportPortalUser user, UpdateIssueSubTypeRQ rq);

	/**
	 * Update {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate} by ID and project ID
	 *
	 * @param projectName             {@link com.epam.ta.reportportal.entity.project.Project#name}
	 * @param updatePatternTemplateRQ {@link UpdatePatternTemplateRQ}
	 * @param user                    {@link ReportPortalUser}
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS updatePatternTemplate(Long id, String projectName, UpdatePatternTemplateRQ updatePatternTemplateRQ,
			ReportPortalUser user);
}
