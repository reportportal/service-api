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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.TestItemTagRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GET operations for {@link TestItem}<br>
 * Default implementation
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 */
@Service
class GetTestItemHandlerImpl implements GetTestItemHandler {
	private final TestItemRepository testItemRepository;
	private final TestItemTagRepository testItemTagRepository;

	@Autowired
	public GetTestItemHandlerImpl(TestItemRepository testItemRepository, TestItemTagRepository testItemTagRepository) {
		this.testItemRepository = testItemRepository;
		this.testItemTagRepository = testItemTagRepository;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.epam.ta.reportportal.core.item.GetTestItemHandler#getTestItem(java
	 * .lang.String)
	 */
	@Override
	public TestItemResource getTestItem(Long testItemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		TestItem testItem = testItemRepository.findById(testItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));
		return TestItemConverter.TO_RESOURCE.apply(testItem);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.epam.ta.reportportal.core.item.GetTestItemHandler#getTestItems(java
	 * .lang.String, java.util.Set, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Iterable<TestItemResource> getTestItems(Filter filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		Page<TestItem> testItems = testItemRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(TestItemConverter.TO_RESOURCE).apply(testItems);
	}

	@Override
	public List<String> getTags(Long launchId, String value, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		return testItemTagRepository.findDistinctByLaunchIdAndValue(launchId, value);
	}

	@Override
	public List<TestItemResource> getTestItems(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<TestItem> testItems = testItemRepository.findAllById(Arrays.asList(ids));
		return testItems.stream().map(TestItemConverter.TO_RESOURCE).collect(Collectors.toList());
	}
}
