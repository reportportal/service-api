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

import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItemResults;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;

import java.time.LocalDateTime;

/**
 * @author Dzianis_Shybeka
 */
public class FakeTestItemResultsGenerator implements FakeDataGenerator<TestItemResults> {

	static final LocalDateTime TEST_ITEM_RESULTS_END_TIME = LocalDateTime.now();
	static final StatusEnum TEST_ITEM_RESULTS_STATUS = StatusEnum.PASSED;

	private LocalDateTime testItemResultsEndTime = TEST_ITEM_RESULTS_END_TIME;
	private StatusEnum status = TEST_ITEM_RESULTS_STATUS;
	private IssueEntity issueEntity = new FakeIssueEntityGenerator().generate();

	private final TestItemResults testItemResults;

	public FakeTestItemResultsGenerator() {

		testItemResults = new TestItemResults();
	}

	public TestItemResults generate() {

		testItemResults.setEndTime(testItemResultsEndTime);
		testItemResults.setStatus(status);
		testItemResults.setIssue(issueEntity);

		return testItemResults;
	}
}
