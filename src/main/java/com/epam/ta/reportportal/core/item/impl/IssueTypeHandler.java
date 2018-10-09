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

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.AMBIGUOUS_TEST_ITEM_STATUS;
import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
@Service
public class IssueTypeHandler {

	private TestItemRepository testItemRepository;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	/**
	 * Verifies that provided test item issue type is valid, and test item
	 * domain object could be processed correctly
	 *
	 * @param locator    Issue locator
	 * @param testItemId Test item id
	 * @param projectId  Project id
	 * @return verified issue type
	 */
	public IssueType defineIssueType(Long testItemId, Long projectId, String locator) {
		return testItemRepository.selectIssueTypeByLocator(projectId, locator)
				.orElseThrow(() -> new ReportPortalException(
						AMBIGUOUS_TEST_ITEM_STATUS, formattedSupplier(
						"Invalid test item issue type definition '{}' is requested for item '{}'. Valid issue types locators are: {}",
						locator, testItemId,
						testItemRepository.selectIssueLocatorsByProject(projectId).stream().map(IssueType::getLocator).collect(toList())
				)));
	}

}
