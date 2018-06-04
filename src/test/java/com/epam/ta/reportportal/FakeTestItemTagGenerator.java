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

import com.epam.ta.reportportal.store.database.entity.item.TestItemTag;

import static com.epam.ta.reportportal.FakeTestItemGenerator.TEST_ITEM_ID;

/**
 * @author Dzianis_Shybeka
 */
public class FakeTestItemTagGenerator implements FakeDataGenerator<TestItemTag> {

	static final Long TEST_TAG_ID = 1233L;
	static final String TEST_TAG_VALUE = "test_tag_value";

	private Long id = TEST_TAG_ID;
	private Long itemId = TEST_ITEM_ID;
	private String tagValue = TEST_TAG_VALUE;

	private final TestItemTag testItemTag;

	public FakeTestItemTagGenerator() {

		testItemTag = new TestItemTag();
	}

	public FakeTestItemTagGenerator withValue(String value) {

		this.tagValue = value;
		return this;
	}

	public TestItemTag generate() {

		testItemTag.setId(id);
		testItemTag.setItemId(itemId);
		testItemTag.setValue(tagValue);

		return testItemTag;
	}
}
