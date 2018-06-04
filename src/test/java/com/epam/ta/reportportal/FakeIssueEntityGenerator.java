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

import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;

import static com.epam.ta.reportportal.FakeTestItemGenerator.TEST_ITEM_ID;

/**
 * @author Dzianis_Shybeka
 */
public class FakeIssueEntityGenerator implements FakeDataGenerator<IssueEntity> {

	static final boolean TEST_AUTO_ANALYZED = true;
	static final boolean TEST_IGNORE_ANALYZER = true;

	private Long issueId = TEST_ITEM_ID;
	private Boolean autoAnalyzed = TEST_AUTO_ANALYZED;
	private Boolean ignoreAnalyzer = TEST_IGNORE_ANALYZER;

	private final IssueEntity issueEntity;

	public FakeIssueEntityGenerator() {

		issueEntity = new IssueEntity();
	}

	public IssueEntity generate() {

		issueEntity.setIssueId(issueId);
		issueEntity.setAutoAnalyzed(autoAnalyzed);
		issueEntity.setIgnoreAnalyzer(ignoreAnalyzer);

		return issueEntity;
	}
}
