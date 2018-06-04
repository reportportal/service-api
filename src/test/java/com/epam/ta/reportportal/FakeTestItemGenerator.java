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

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.store.database.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.store.database.entity.item.*;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

/**
 * @author Dzianis_Shybeka
 */
public class FakeTestItemGenerator implements FakeDataGenerator<TestItem> {

	public static final Long TEST_ITEM_ID = 123L;
	public static final String TEST_DESCRIPTION = "test description";
	public static final String TEST_UNIQUE_ID = "test unique id";
	public static final String TEST_ITEM_NAME = "item name";
	public static final LocalDateTime TEST_START_TIME = LocalDateTime.now();
	public static final TestItemTypeEnum TEST_TYPE = TestItemTypeEnum.TEST;

	private TestItemTypeEnum type = TEST_TYPE;

	private TestItemTag tag1 = new FakeTestItemTagGenerator().generate();
	private TestItemTag tag2 = new FakeTestItemTagGenerator().withValue("tag2").generate();
	private HashSet<TestItemTag> tags = Sets.newHashSet(tag1, tag2);

	private TestItemResults testItemResults = new FakeTestItemResultsGenerator().generate();

	private TestItemStructure testItemStructure = new FakeTestItemStructureGenerator().generate();

	private List<Parameter> parameters = Lists.newArrayList(new FakeParameterGenerator().generate());

	private Launch launch = new FakeLaunchGenerator().generate();

	private final TestItem testItem;

	public FakeTestItemGenerator() {

		testItem = new TestItem();
	}

	public FakeTestItemGenerator withParameters(List<Parameter> parameters) {

		this.parameters = parameters;

		return this;
	}

	public FakeTestItemGenerator withType(TestItemTypeEnum type) {

		this.type = type;

		return this;
	}

	public FakeTestItemGenerator withTestItemResults(TestItemResults testItemResults) {

		this.testItemResults = testItemResults;

		return this;
	}

	@Override
	public TestItem generate() {

		testItem.setItemId(TEST_ITEM_ID);
		testItem.setDescription(TEST_DESCRIPTION);
		testItem.setUniqueId(TEST_UNIQUE_ID);
		testItem.setTags(tags);

		testItem.setTestItemResults(testItemResults);

		testItem.setParameters(parameters);

		testItem.setName(TEST_ITEM_NAME);
		testItem.setStartTime(TEST_START_TIME);

		testItem.setType(type);

		testItem.setTestItemStructure(testItemStructure);

		testItem.setLaunch(launch);

		return testItem;
	}
}
