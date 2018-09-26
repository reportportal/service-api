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
package com.epam.ta.reportportal.core.imprt.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;

import java.io.File;

/**
 * Handler for processing launch importing.
 *
 * @author Pavel_Bortnik
 */
public interface ImportStrategy {
	/**
	 * Processing launch importing.
	 *
	 * @param projectDetails project
	 * @param user           user
	 * @param file           zip file that contains xml test reports
	 * @return launch id
	 */
	Long importLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, File file);
}
