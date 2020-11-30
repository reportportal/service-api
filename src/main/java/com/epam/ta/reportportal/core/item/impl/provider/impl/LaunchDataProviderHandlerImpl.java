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

package com.epam.ta.reportportal.core.item.impl.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.impl.provider.DataProviderHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class LaunchDataProviderHandlerImpl implements DataProviderHandler {

	private static final String LAUNCH_ID_PARAM = "launchId";

	@Autowired
	private LaunchAccessValidator launchAccessValidator;

	@Autowired
	private TestItemRepository testItemRepository;

	@Override
	public Page<TestItem> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, String providerType, Map<String, String> providerParams) {
		Long launchId = Optional.ofNullable(providerParams.get(LAUNCH_ID_PARAM)).map(Long::parseLong)
				.orElseThrow(() -> new ReportPortalException(
						ErrorType.BAD_REQUEST_ERROR,
						"Launch id must be provided for launch based items provider"
				));
		launchAccessValidator.validate(launchId, projectDetails, user);
		return testItemRepository.findByFilter(filter, pageable);
	}
}
