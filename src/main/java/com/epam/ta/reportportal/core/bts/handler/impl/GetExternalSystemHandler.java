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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.ta.reportportal.core.bts.handler.IGetExternalSystemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link IGetExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class GetExternalSystemHandler implements IGetExternalSystemHandler {

	@Autowired
	private BugTrackingSystemRepository externalSystemRepository;

	@Override
	public ExternalSystemResource getExternalSystem(String projectName, Long id) {
		BugTrackingSystem bugTrackingSystem = externalSystemRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.EXTERNAL_SYSTEM_NOT_FOUND, id));
		return ExternalSystemConverter.TO_RESOURCE.apply(bugTrackingSystem);
	}
}