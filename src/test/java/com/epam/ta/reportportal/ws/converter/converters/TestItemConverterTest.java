/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.FakeTestItemGenerator;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static com.epam.ta.reportportal.FakeTestItemGenerator.TEST_ITEM_ID;
import static com.epam.ta.reportportal.FakeTestItemStructureGenerator.TEST_PARENT_ITEM_ID;
import static com.epam.ta.reportportal.store.commons.EntityUtils.TO_DATE;
import static org.junit.Assert.*;

/**
 * @author Dzianis_Shybeka
 */
public class TestItemConverterTest {

	@Test
	public void toResource() throws Exception {
		//  given:

		TestItem testItem = new FakeTestItemGenerator().generate();

		//  when:
		TestItemResource testItemResource = TestItemConverter.TO_RESOURCE.apply(testItem);

		//  then:
		assertNotNull(testItemResource);

		assertEquals(String.valueOf(TEST_ITEM_ID), testItemResource.getItemId());
		assertEquals(testItem.getDescription(), testItemResource.getDescription());
		assertEquals(testItem.getUniqueId(), testItemResource.getUniqueId());

		assertEquals(testItem.getTags().size(), testItemResource.getTags().size());
		assertFalse(testItem.getTags().stream().anyMatch(tag -> !testItemResource.getTags().contains(tag.getValue())));

		assertEquals(TO_DATE.apply(testItem.getTestItemResults().getEndTime()), testItemResource.getEndTime());
		assertEquals(testItem.getTestItemResults().getStatus().toString(), testItemResource.getStatus());
		assertEquals(testItem.getType().toString(), testItemResource.getType());
		assertEquals(String.valueOf(TEST_PARENT_ITEM_ID), testItemResource.getParent());
		assertEquals(String.valueOf(testItem.getLaunch().getId()), testItemResource.getLaunchId());

		assertEquals(testItem.getParameters().get(0).getKey(), testItemResource.getParameters().get(0).getKey());
		assertEquals(testItem.getParameters().get(0).getValue(), testItemResource.getParameters().get(0).getValue());
	}

	@Test
	public void toResource_without_parameters_type_and_results_not_failing() throws Exception {
		//  given:

		TestItem testItem = new FakeTestItemGenerator().withParameters(null).withType(null).withTestItemResults(null).generate();

		//  when:
		TestItemResource testItemResource = TestItemConverter.TO_RESOURCE.apply(testItem);

		//  then:
		assertNotNull(testItemResource);

		assertEquals(String.valueOf(TEST_ITEM_ID), testItemResource.getItemId());
		assertEquals(testItem.getDescription(), testItemResource.getDescription());
		assertEquals(testItem.getUniqueId(), testItemResource.getUniqueId());

		assertEquals(testItem.getTags().size(), testItemResource.getTags().size());
		assertFalse(testItem.getTags().stream().anyMatch(tag -> !testItemResource.getTags().contains(tag.getValue())));

		assertEquals(String.valueOf(TEST_PARENT_ITEM_ID), testItemResource.getParent());
		assertEquals(String.valueOf(testItem.getLaunch().getId()), testItemResource.getLaunchId());

		assertNull(testItemResource.getParameters());
		assertNull(testItemResource.getEndTime());
		assertNull(testItemResource.getType());

		assertTrue(StringUtils.isEmpty(testItemResource.getStatus()));
	}
}
