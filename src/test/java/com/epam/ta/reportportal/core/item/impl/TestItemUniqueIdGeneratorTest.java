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
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class TestItemUniqueIdGeneratorTest {

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private TestItemUniqueIdGenerator uniqueIdGenerator;

	@Test
	void validateTest() {
		assertFalse(uniqueIdGenerator.validate(""));
		assertFalse(uniqueIdGenerator.validate(null));
		assertFalse(uniqueIdGenerator.validate("qwerty"));
		assertTrue(uniqueIdGenerator.validate("auto: 123456789"));
	}

	@Test
	void generateTest() {
		Launch launch = new Launch();
		launch.setProjectId(1L);
		launch.setName("launchName");

		TestItem testItem = new TestItem();
		testItem.setName("itemName");
		testItem.setPath("1.2.3");

		HashMap<Long, String> pathNamesMap = new HashMap<>();
		pathNamesMap.put(1L, "first");
		pathNamesMap.put(2L, "second");
		pathNamesMap.put(3L, "third");

		Parameter param1 = new Parameter();
		param1.setKey("key1");
		param1.setValue("val1");
		Parameter param2 = new Parameter();
		param1.setKey("key2");
		param1.setValue("val2");
		testItem.setParameters(Sets.newHashSet(param1, param2));

		when(testItemRepository.selectPathNames(testItem.getPath())).thenReturn(pathNamesMap);
		String generated = uniqueIdGenerator.generate(testItem, launch);

		assertNotNull(generated);
		assertTrue(generated.startsWith("auto:"));
	}
}