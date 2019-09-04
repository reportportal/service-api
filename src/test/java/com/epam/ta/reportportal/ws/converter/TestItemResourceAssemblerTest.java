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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.entity.item.ItemPathName;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.ws.converter.helper.TestItemCreationHelper;
import com.epam.ta.reportportal.ws.model.TestItemHistoryResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
@ExtendWith(MockitoExtension.class)
class TestItemResourceAssemblerTest {

	private static final Double DURATION = 0.75355;

	@InjectMocks
	private TestItemResourceAssembler testItemResourceAssembler;

	@Test
	void toHistoryResourceShouldReturnHistoryResourceWithPathNameWhenPathNameIsNotNull() {
		//GIVEN
		TestItem testItem = TestItemCreationHelper.prepareTestItem();
		TestItemResults itemResults = testItem.getItemResults();
		itemResults.setDuration(DURATION);

		PathName pathName = preparePathName();

		//WHEN
		TestItemHistoryResource historyResource = testItemResourceAssembler.toHistoryResource(testItem, pathName);

		//THEN
		assertNotNull(historyResource);
		assertNotNull(historyResource.getPathNames());
	}

	private PathName preparePathName() {
		PathName pathName = new PathName();

		List<ItemPathName> itemPaths = new ArrayList<>();
		ItemPathName itemPathName = new ItemPathName();
		itemPathName.setId(1L);
		itemPathName.setName("Launch 1 Suite");
		pathName.setItemPaths(itemPaths);

		return pathName;
	}

	@Test
	void toHistoryResourceShouldReturnHistoryResourceWithoutPathNameWhenPathNameIsNull() {
		//GIVEN
		TestItem testItem = TestItemCreationHelper.prepareTestItem();
		TestItemResults itemResults = testItem.getItemResults();
		itemResults.setDuration(DURATION);

		//WHEN
		TestItemHistoryResource historyResource = testItemResourceAssembler.toHistoryResource(testItem, null);

		//THEN
		assertNotNull(historyResource);
		assertNull(historyResource.getPathNames());
	}
}
