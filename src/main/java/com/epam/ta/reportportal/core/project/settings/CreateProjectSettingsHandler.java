/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeCreatedRS;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface CreateProjectSettingsHandler {

	/**
	 * Create issue sub-type for specified project
	 *
	 * @param projectDetails
	 * @param user
	 * @param rq
	 * @return EntryCreatedRS
	 */
	IssueSubTypeCreatedRS createProjectIssueSubType(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			CreateIssueSubTypeRQ rq);

	/**
	 * @param projectDetails
	 * @param createPatternTemplateRQ
	 * @return
	 */
	EntryCreatedRS createPatternTemplate(ReportPortalUser.ProjectDetails projectDetails, CreatePatternTemplateRQ createPatternTemplateRQ);
}
