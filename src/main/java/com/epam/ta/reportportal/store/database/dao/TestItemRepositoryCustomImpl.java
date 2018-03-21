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
import com.epam.ta.reportportal.store.database.entity.item.TestItemResults;
import com.epam.ta.reportportal.store.database.entity.item.TestItemStructure;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;
import com.epam.ta.reportportal.store.jooq.Tables;
import com.epam.ta.reportportal.store.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.store.jooq.tables.JTestItem;
import com.epam.ta.reportportal.store.jooq.tables.JTestItemResults;
import com.epam.ta.reportportal.store.jooq.tables.JTestItemStructure;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.store.jooq.Tables.*;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.*;

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

	public Map<Long, String> selectPathNames(Long itemId) {
		JTestItemStructure tis = TEST_ITEM_STRUCTURE.as("tis");
		JTestItem ti = TEST_ITEM.as("ti");
		return dsl.withRecursive("p")
				.as(dsl.select(TEST_ITEM_STRUCTURE.ITEM_ID, TEST_ITEM_STRUCTURE.PARENT_ID, TEST_ITEM.NAME)
						.from(TEST_ITEM_STRUCTURE)
						.join(TEST_ITEM)
						.onKey()
						.where(TEST_ITEM_STRUCTURE.ITEM_ID.eq(itemId))
						.unionAll(dsl.select(tis.ITEM_ID, tis.PARENT_ID, ti.NAME)
								.from(tis)
								.join(ti)
								.onKey()
								.join(name("p"))
								.on(tis.ITEM_ID.eq(field(name("p", "parent_id"), Long.class)))))
				.select()
				.from(name("p"))
				.fetch()
				.intoMap(field(name("item_id"), Long.class), field(name("name"), String.class));
	}

	@Override
	public List<TestItem> selectItemsInStatusByLaunch(Long launchId, StatusEnum... statuses) {
		List<JStatusEnum> jStatuses = Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name())).collect(toList());
		return commonTestItemDslSelect().where(TEST_ITEM.LAUNCH_ID.eq(launchId).and(TEST_ITEM_RESULTS.STATUS.in(jStatuses)))
				.fetch(this::fetchTestItem);
	}

	@Override
	public List<TestItem> selectItemsInStatusByParent(Long itemId, StatusEnum... statuses) {
		List<JStatusEnum> jStatuses = Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name())).collect(toList());
		return commonTestItemDslSelect().where(TEST_ITEM_STRUCTURE.PARENT_ID.eq(itemId).and(TEST_ITEM_RESULTS.STATUS.in(jStatuses)))
				.fetch(this::fetchTestItem);
	}

	@Override
	public Boolean hasItemsInStatusByLaunch(Long launchId, StatusEnum... statuses) {
		List<JStatusEnum> jStatuses = Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name())).collect(toList());
		return dsl.fetchExists(dsl.selectOne()
				.from(TEST_ITEM)
				.join(TEST_ITEM_RESULTS)
				.onKey()
				.where(TEST_ITEM.LAUNCH_ID.eq(launchId))
				.and(TEST_ITEM_RESULTS.STATUS.in(jStatuses)));
	}

	@Override
	public Boolean hasItemsInStatusByParent(Long parentId, StatusEnum... statuses) {
		List<JStatusEnum> jStatuses = Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name())).collect(toList());
		return dsl.fetchExists(
				commonTestItemDslSelect().where(TEST_ITEM_STRUCTURE.PARENT_ID.eq(parentId)).and(TEST_ITEM_RESULTS.STATUS.in(jStatuses)));
	}

	@Override
	public List<Long> selectIdsNotInIssueByLaunch(Long launchId, String issueType) {
		return commonTestItemDslSelect().join(ISSUE)
				.on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.ITEM_ID))
				.join(ISSUE_TYPE)
				.on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
				.where(TEST_ITEM.LAUNCH_ID.eq(launchId))
				.and(ISSUE_TYPE.LOCATOR.ne(issueType))
				.fetchInto(Long.class);
	}

	@Override
	public List<TestItem> selectItemsInIssueByLaunch(Long launchId, String issueType) {
		return commonTestItemDslSelect().join(ISSUE)
				.on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.ITEM_ID))
				.join(ISSUE_TYPE)
				.on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
				.where(TEST_ITEM.LAUNCH_ID.eq(launchId))
				.and(ISSUE_TYPE.LOCATOR.eq(issueType))
				.fetch(r -> {
					TestItem item = fetchTestItem(r);
					item.getTestItemResults().setIssue(r.into(IssueEntity.class));
					return item;
				});
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
						.and(TEST_ITEM_RESULTS.STATUS.eq(JStatusEnum.FAILED).or(TEST_ITEM_RESULTS.STATUS.eq(JStatusEnum.SKIPPED))))) ?
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
				.on(PROJECT_CONFIGURATION.ID.eq(PROJECT.ID))
				.join(ISSUE_TYPE_PROJECT_CONFIGURATION)
				.on(PROJECT_CONFIGURATION.ID.eq(ISSUE_TYPE_PROJECT_CONFIGURATION.CONFIGURATION_ID))
				.join(Tables.ISSUE_TYPE)
				.on(ISSUE_TYPE_PROJECT_CONFIGURATION.ISSUE_TYPE_ID.eq(Tables.ISSUE_TYPE.ID))
				.fetchInto(IssueType.class);
	}

	@Override
	public void interruptInProgressItems(Long launchId) {
		JTestItemResults res = TEST_ITEM_RESULTS.as("res");
		JTestItem ts = TEST_ITEM.as("ts");
		dsl.update(res)
				.set(res.STATUS, JStatusEnum.INTERRUPTED)
				.set(res.DURATION, extractEpochFrom(DSL.timestampDiff(currentTimestamp(), ts.START_TIME)))
				.from(ts)
				.where(ts.LAUNCH_ID.eq(launchId))
				.and(res.ITEM_ID.eq(ts.ITEM_ID))
				.and(res.STATUS.eq(JStatusEnum.IN_PROGRESS))
				.execute();
	}

	@Override
	public IssueType selectIssueTypeByLocator(Long projectId, String locator) {
		return dsl.select()
				.from(ISSUE_TYPE)
				.join(ISSUE_TYPE_PROJECT_CONFIGURATION)
				.on(ISSUE_TYPE.ID.eq(ISSUE_TYPE_PROJECT_CONFIGURATION.ISSUE_TYPE_ID))
				.where(ISSUE_TYPE_PROJECT_CONFIGURATION.CONFIGURATION_ID.eq(projectId))
				.and(ISSUE_TYPE.LOCATOR.eq(locator))
				.fetchOne()
				.into(IssueType.class);
	}

	/**
	 * Extracts duration in seconds from interval.
	 *
	 * @param field Interval
	 * @return Duration in seconds
	 */
	private static Field<Double> extractEpochFrom(Field<DayToSecond> field) {
		return DSL.field("extract(epoch from {0})", Double.class, field);
	}

	/**
	 * Commons select of an item with it's results and structure
	 *
	 * @return Select condition step
	 */

	private SelectOnConditionStep<Record> commonTestItemDslSelect() {
		return dsl.select()
				.from(TEST_ITEM)
				.join(TEST_ITEM_STRUCTURE)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_STRUCTURE.ITEM_ID))
				.join(TEST_ITEM_RESULTS)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.ITEM_ID));
	}

	/**
	 * Fetching record results into Test item object.
	 *
	 * @param r Record
	 * @return Test Item
	 */
	private TestItem fetchTestItem(Record r) {
		TestItem testItem = r.into(TestItem.class);
		testItem.setTestItemStructure(r.into(TestItemStructure.class));
		testItem.setTestItemResults(r.into(TestItemResults.class));
		return testItem;
	}

}
