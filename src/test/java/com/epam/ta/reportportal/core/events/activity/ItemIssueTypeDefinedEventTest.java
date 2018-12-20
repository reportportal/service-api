/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import org.junit.Test;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.OBJECT_ID;
import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.PROJECT_ID;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemIssueTypeDefinedEventTest {

	@Test
	public void toActivity() {
	}

	private static TestItemActivityResource getTestItem() {
		TestItemActivityResource testItem = new TestItemActivityResource();
		testItem.setProjectId(PROJECT_ID);
		testItem.setStatus("FAILED");
		testItem.setIssueTypeLongName("Product Bug");
		testItem.setIssueDescription("Description");
		testItem.setIgnoreAnalyzer(false);
		testItem.setAutoAnalyzed(true);
		testItem.setName("name");
		testItem.setId(OBJECT_ID);
		testItem.setTickets("1:http:/example.com/ticket/1,2:http:/example.com/ticket/2");
		return testItem;
	}
}