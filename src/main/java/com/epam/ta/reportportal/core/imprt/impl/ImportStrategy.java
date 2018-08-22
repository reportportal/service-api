/*
 * Copyright 2017 EPAM Systems
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
