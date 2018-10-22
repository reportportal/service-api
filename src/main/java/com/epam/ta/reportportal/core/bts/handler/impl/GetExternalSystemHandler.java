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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.ta.reportportal.core.bts.handler.IGetExternalSystemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
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
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));
		return ExternalSystemConverter.TO_RESOURCE.apply(bugTrackingSystem);
	}
}