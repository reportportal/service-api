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

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
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
