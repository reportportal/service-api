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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.store.commons.Predicates;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.hibernate.persister.entity.Queryable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
	private final TestItemResourceAssembler itemAssembler;

	@Autowired
	public GetTestItemHandlerImpl(TestItemRepository testItemRepository, TestItemResourceAssembler itemAssembler) {
		this.testItemRepository = testItemRepository;
		this.itemAssembler = itemAssembler;
	}

	@Override
	public TestItemResource getTestItem(String testItemId) {

		Optional<TestItem> maybeTestItem = testItemRepository.findById(Long.valueOf(testItemId));

		BusinessRule.expect(maybeTestItem, Predicates.isPresent()).verify(ErrorType.TEST_ITEM_NOT_FOUND, testItemId);

		return itemAssembler.toResource(maybeTestItem.get());
	}

	@Override
	public Iterable<TestItemResource> getTestItems(Queryable filterable, Pageable pageable) {
		return null;
	}

	@Override
	public List<String> getTags(String launchId, String value) {
		return null;
	}

	@Override
	public List<TestItemResource> getTestItems(String[] ids) {
		return null;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 *
	//	 * @see
	//	 * com.epam.ta.reportportal.core.item.GetTestItemHandler#getTestItems(java
	//	 * .lang.String, java.util.Set, org.springframework.data.domain.Pageable)
	//	 */
	//	@Override
	//	public Iterable<TestItemResource> getTestItems(Queryable filterable, Pageable pageable) {
	//		return itemAssembler.toPagedResources(testItemRepository.findByFilter(filterable, pageable));
	//	}
	//
	//	@Override
	//	public List<String> getTags(String launchId, String value) {
	//		return testItemRepository.findDistinctValues(launchId, value, "tags");
	//	}
	//
	//	@Override
	//	public List<TestItemResource> getTestItems(String[] ids) {
	//		Iterable<TestItem> testItems = testItemRepository.findAll(Arrays.asList(ids));
	//		return itemAssembler.toResources(testItems);
	//	}
}
