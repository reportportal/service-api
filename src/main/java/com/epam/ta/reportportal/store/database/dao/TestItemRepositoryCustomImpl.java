/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemCommon;
import com.epam.ta.reportportal.store.database.entity.item.TestItemResults;
import com.epam.ta.reportportal.store.database.entity.item.TestItemStructure;
import com.epam.ta.reportportal.store.database.entity.item.issue.Issue;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;
import com.epam.ta.reportportal.store.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.epam.ta.reportportal.store.jooq.Tables.*;
import static com.epam.ta.reportportal.store.jooq.tables.Issue.ISSUE;
import static com.epam.ta.reportportal.store.jooq.tables.IssueType.ISSUE_TYPE;
import static com.epam.ta.reportportal.store.jooq.tables.TestItem.TEST_ITEM;
import static com.epam.ta.reportportal.store.jooq.tables.TestItemResults.TEST_ITEM_RESULTS;
import static com.epam.ta.reportportal.store.jooq.tables.TestItemStructure.TEST_ITEM_STRUCTURE;

/**
 * @author Pavel Bortnik
 */
@Repository
public class TestItemRepositoryCustomImpl implements TestItemRepositoryCustom {

	private DSLContext dsl;

	@Autowired
	public void setDsl(DSLContext dsl) {
		this.dsl = dsl;
	}

	@Override
	public List<TestItemCommon> selectItemsInStatusByLaunch(Long launchId, StatusEnum status) {
		com.epam.ta.reportportal.store.jooq.enums.StatusEnum statusEnum = com.epam.ta.reportportal.store.jooq.enums.StatusEnum.valueOf(
				status.name());
		return commonTestItemDslSelect().where(TEST_ITEM_STRUCTURE.LAUNCH_ID.eq(launchId).and(TEST_ITEM_RESULTS.STATUS.eq(statusEnum)))
				.fetch(r -> {
					TestItemCommon testItemCommon = new TestItemCommon();
					testItemCommon.setTestItem(r.into(TestItem.class));
					testItemCommon.setTestItemStructure(r.into(TestItemStructure.class));
					testItemCommon.setTestItemResults(r.into(TestItemResults.class));
					return testItemCommon;
				});
	}

	@Override
	public List<Long> selectIdsNotInIssueByLaunch(Long launchId, String issueType) {
		return dsl.select(TEST_ITEM.ITEM_ID)
				.from(TEST_ITEM)
				.join(TEST_ITEM_STRUCTURE)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_STRUCTURE.ITEM_ID))
				.join(TEST_ITEM_RESULTS)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.ITEM_ID))
				.join(ISSUE)
				.on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.ITEM_ID))
				.join(ISSUE_TYPE)
				.on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
				.where(TEST_ITEM_STRUCTURE.LAUNCH_ID.eq(launchId))
				.and(ISSUE_TYPE.LOCATOR.ne(issueType))
				.fetchInto(Long.class);
	}

	@Override
	public List<TestItemCommon> selectItemsInIssueByLaunch(Long launchId, String issueType) {
		return commonTestItemDslSelect().join(ISSUE).on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.ITEM_ID))
				.join(ISSUE_TYPE)
				.on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
				.where(TEST_ITEM_STRUCTURE.LAUNCH_ID.eq(launchId))
				.and(ISSUE_TYPE.LOCATOR.eq(issueType))
				.fetch(r -> new TestItemCommon(r.into(TestItem.class), r.into(TestItemResults.class), r.into(TestItemStructure.class),
						r.into(Issue.class), r.into(IssueType.class)
				));
	}

	@Override
	public StatusEnum identifyStatus(Long testItemId) {
		return dsl.fetchExists(dsl.selectOne()
				.from(TEST_ITEM)
				.join(TEST_ITEM_STRUCTURE)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_STRUCTURE.ITEM_ID))
				.join(TEST_ITEM_RESULTS)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.ITEM_ID))
				.where(TEST_ITEM_STRUCTURE.PARENT_ID.eq(testItemId)
						.and(TEST_ITEM_RESULTS.STATUS.eq(com.epam.ta.reportportal.store.jooq.enums.StatusEnum.FAILED)
								.or(TEST_ITEM_RESULTS.STATUS.eq(com.epam.ta.reportportal.store.jooq.enums.StatusEnum.SKIPPED))))) ?
				StatusEnum.FAILED :
				StatusEnum.PASSED;
	}

	@Override
	public boolean hasChildren(Long testItemId) {
		return dsl.fetchExists(dsl.selectOne()
				.from(TEST_ITEM)
				.join(TEST_ITEM_STRUCTURE)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_STRUCTURE.ITEM_ID))
				.where(TEST_ITEM_STRUCTURE.PARENT_ID.eq(testItemId)));
	}

	@Override
	public List<IssueType> selectIssueLocatorsByProject(Long projectId) {
		return dsl.select()
				.from(PROJECT)
				.join(PROJECT_CONFIGURATION)
				.on(PROJECT.PROJECT_CONFIGURATION_ID.eq(PROJECT_CONFIGURATION.ID))
				.join(ISSUE_TYPE_PROJECT_CONFIGURATION)
				.on(PROJECT_CONFIGURATION.ID.eq(ISSUE_TYPE_PROJECT_CONFIGURATION.CONFIGURATION_ID))
				.join(Tables.ISSUE_TYPE)
				.on(ISSUE_TYPE_PROJECT_CONFIGURATION.ISSUE_TYPE_ID.eq(Tables.ISSUE_TYPE.ID))
				.fetchInto(IssueType.class);
	}

	private SelectOnConditionStep<Record> commonTestItemDslSelect() {
		return dsl.select()
				.from(TEST_ITEM)
				.join(TEST_ITEM_STRUCTURE).on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_STRUCTURE.ITEM_ID))
				.join(TEST_ITEM_RESULTS).on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.ITEM_ID));
	}

}
