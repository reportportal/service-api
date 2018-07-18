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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.CompositeFilter;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.Queryable;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemResource;
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
	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final TestItemResourceAssembler itemAssembler;

	public GetTestItemHandlerImpl(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			TestItemResourceAssembler itemAssembler) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.itemAssembler = itemAssembler;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.epam.ta.reportportal.core.item.GetTestItemHandler#getTestItem(java
		 * .lang.String)
		 */
	@Override
	public TestItemResource getTestItem(String testItemId) {
		TestItem testItem = testItemRepository.findOne(testItemId);
		BusinessRule.expect(testItem, Predicates.notNull()).verify(ErrorType.TEST_ITEM_NOT_FOUND, testItemId);
		return itemAssembler.toResource(testItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.epam.ta.reportportal.core.item.GetTestItemHandler#getTestItems(java
	 * .lang.String, java.util.Set, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Iterable<TestItemResource> getTestItems(Queryable filterable, Pageable pageable, String projectName) {
		/*
		 * If request came without filter at launchRef's, than should be created a new filter with
		 * launch ids of specified project.
		 */
		if (filterable.toCriteria().stream().noneMatch(it -> it.getKey().equalsIgnoreCase("launchRef"))) {
			String launchIds = launchRepository.findLaunchIdsByProjectId(projectName)
					.stream()
					.map(Launch::getId)
					.collect(Collectors.joining(","));
			filterable = new CompositeFilter(filterable, new Filter(TestItem.class, Condition.IN, false, launchIds, "launch"));
		}
		return itemAssembler.toPagedResources(testItemRepository.findByFilter(filterable, pageable));
	}

	@Override
	public List<String> getTags(String launchId, String value) {
		return testItemRepository.findDistinctValues(launchId, value, "tags");
	}

	@Override
	public List<TestItemResource> getTestItems(String[] ids) {
		Iterable<TestItem> testItems = testItemRepository.findAll(Arrays.asList(ids));
		return itemAssembler.toResources(testItems);
	}
}
