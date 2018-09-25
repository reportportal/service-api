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

package com.epam.ta.reportportal.core.item.impl;

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
	public TestItemResource getTestItem(Long testItemId) {
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
	public Iterable<TestItemResource> getTestItems(Filter filter, Pageable pageable) {
		Page<TestItem> testItems = testItemRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(TestItemConverter.TO_RESOURCE).apply(testItems);
	}

	@Override
	public List<String> getTags(Long launchId, String value) {
		return testItemTagRepository.findDistinctByLaunchIdAndValue(launchId, value);
	}

	@Override
	public List<TestItemResource> getTestItems(Long[] ids) {
		List<TestItem> testItems = testItemRepository.findAllById(Arrays.asList(ids));
		return testItems.stream().map(TestItemConverter.TO_RESOURCE).collect(Collectors.toList());
	}
}
