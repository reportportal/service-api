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

package com.epam.ta.reportportal.core.item.impl.history.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import com.epam.ta.reportportal.core.item.impl.history.provider.HistoryProvider;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * * Required for retrieving {@link TestItemHistory} content.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class TestItemBaselineHistoryProvider implements HistoryProvider {

	private final TestItemService testItemService;
	private final LaunchAccessValidator launchAccessValidator;
	private final TestItemRepository testItemRepository;

	@Autowired
	public TestItemBaselineHistoryProvider(TestItemService testItemService, LaunchAccessValidator launchAccessValidator,
			TestItemRepository testItemRepository) {
		this.testItemService = testItemService;
		this.launchAccessValidator = launchAccessValidator;
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Page<TestItemHistory> provide(Queryable filter, Pageable pageable, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, boolean usingHash) {

		return historyRequestParams.getParentId()
				.map(itemId -> loadHistory(filter, pageable, itemId, historyRequestParams, projectDetails, user, usingHash))
				.orElseGet(() -> historyRequestParams.getItemId()
						.map(itemId -> loadHistory(filter, pageable, itemId, historyRequestParams, projectDetails, user, usingHash))
						.orElseGet(() -> Page.empty(pageable)));
	}

	private Page<TestItemHistory> loadHistory(Queryable filter, Pageable pageable, Long itemId, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, boolean usingHash) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		Launch launch = testItemService.getEffectiveLaunch(testItem);
		launchAccessValidator.validate(launch.getId(), projectDetails, user);

		return historyRequestParams.getHistoryType()
				.filter(HistoryRequestParams.HistoryTypeEnum.LINE::equals)
				.map(type -> testItemRepository.loadItemsHistoryPage(filter,
						pageable,
						projectDetails.getProjectId(),
						launch.getName(),
						historyRequestParams.getHistoryDepth(),
						usingHash
				))
				.orElseGet(() -> testItemRepository.loadItemsHistoryPage(filter,
						pageable,
						projectDetails.getProjectId(),
						historyRequestParams.getHistoryDepth(),
						usingHash
				));

	}
}
