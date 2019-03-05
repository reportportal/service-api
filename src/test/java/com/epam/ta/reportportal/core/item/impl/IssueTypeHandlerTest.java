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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class IssueTypeHandlerTest {

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private IssueTypeHandler issueTypeHandler;

	@Test
	void defineIssueTypeWithNullLocator() {
		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> issueTypeHandler.defineIssueType(1L, 2L, null));
		assertEquals("Locator should not be null", exception.getMessage());
	}

	@Test
	void defineNotExistIssueType() {
		when(testItemRepository.selectIssueTypeByLocator(2L, "not_exist")).thenReturn(Optional.empty());
		IssueType issueType = new IssueType();
		issueType.setLocator("exists");
		when(testItemRepository.selectIssueLocatorsByProject(2L)).thenReturn(Collections.singletonList(issueType));

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> issueTypeHandler.defineIssueType(1L, 2L, "not_exist")
		);
		assertEquals(
				"Test item status is ambiguous. Invalid test item issue type definition 'not_exist' is requested for item '1'. Valid issue types locators are: [exists]",
				exception.getMessage()
		);
	}
}