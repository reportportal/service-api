/*
 * Copyright 2020 EPAM Systems
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

import com.epam.ta.reportportal.core.item.identity.TestCaseHashGeneratorImpl;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class TestCaseHashGeneratorImplTest {

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private TestCaseHashGeneratorImpl testCaseHashGenerator;

	@Test
	void sameHashesForSameObjectsTest() {
		TestItem item = getItem();

		HashMap<Long, String> pathNames = new HashMap<>();
		pathNames.put(1L, "suite");
		pathNames.put(2L, "test");

		when(testItemRepository.selectPathNames(item.getPath())).thenReturn(pathNames);

		Integer first = testCaseHashGenerator.generate(item, 100L);
		Integer second = testCaseHashGenerator.generate(item, 100L);

		assertNotNull(first);
		assertNotNull(second);
		assertEquals(first, second);
	}

	private TestItem getItem() {
		TestItem item = new TestItem();
		item.setName("item");
		HashSet<Parameter> parameters = new HashSet<>();
		Parameter parameter = new Parameter();
		parameter.setKey("key");
		parameter.setValue("value");
		parameters.add(parameter);
		item.setParameters(parameters);
		item.setPath("1.2.3");
		return item;
	}
}