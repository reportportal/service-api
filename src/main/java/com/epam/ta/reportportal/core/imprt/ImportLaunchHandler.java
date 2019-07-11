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
package com.epam.ta.reportportal.core.imprt;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Pavel_Bortnik
 */
public interface ImportLaunchHandler {

	/**
	 * Import launch from file with specified format.
	 *
	 * @param projectDetails Project Details
	 * @param user  user
	 * @param format    report format
	 * @param file      file with report
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS importLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, String format, MultipartFile file);
}