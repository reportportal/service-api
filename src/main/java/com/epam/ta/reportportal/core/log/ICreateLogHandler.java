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

package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Create {@link Log} request handler
 *
 * @author Henadzi_Vrubleuski
 */
public interface ICreateLogHandler {

	/**
	 * Creates {@link Log} instance and save binary data related to this log
	 *
	 * @param createLogRQ
	 * @return
	 * @throws ReportPortalException
	 */
	@Nonnull
	EntryCreatedRS createLog(@Nonnull SaveLogRQ createLogRQ, @Nullable MultipartFile file, @Nullable String projectName);
}
