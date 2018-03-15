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

import com.epam.ta.reportportal.store.database.entity.launch.ExecutionStatistics;
import com.epam.ta.reportportal.store.database.entity.launch.LaunchFull;
import com.epam.ta.reportportal.store.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.store.jooq.tables.JLaunch;
import com.epam.ta.reportportal.store.jooq.tables.JTestItem;
import com.epam.ta.reportportal.store.jooq.tables.JTestItemResults;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.epam.ta.reportportal.store.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

/**
 * @author Pavel Bortnik
 */
@Repository
public class LaunchRepositoryCustomImpl implements LaunchRepositoryCustom {

	private DSLContext dsl;

	@Autowired
	public void setDsl(DSLContext dsl) {
		this.dsl = dsl;
	}

	@Override
	public Boolean hasItems(Long launchId) {
		return dsl.fetchExists(dsl.selectOne().from(TEST_ITEM).where(TEST_ITEM.LAUNCH_ID.eq(launchId)));
	}

	@Override
	public Boolean identifyStatus(Long launchId) {
		return dsl.fetchExists(dsl.selectOne()
				.from(TEST_ITEM)
				.join(TEST_ITEM_RESULTS)
				.on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.ITEM_ID))
				.where(TEST_ITEM.LAUNCH_ID.eq(launchId)
						.and(TEST_ITEM_RESULTS.STATUS.eq(JStatusEnum.FAILED).or(TEST_ITEM_RESULTS.STATUS.eq(JStatusEnum.SKIPPED)))));
	}

	@Override
	public List<LaunchFull> fullLaunchWithStatistics() {
		JLaunch l = LAUNCH.as("l");
		JTestItem ti = TEST_ITEM.as("ti");
		JTestItemResults tr = TEST_ITEM_RESULTS.as("tr");
		return dsl.select(l.ID, l.PROJECT_ID, l.USER_ID, l.NAME, l.DESCRIPTION, l.START_TIME, l.NUMBER, l.LAST_MODIFIED, l.MODE,
				sum(when(tr.STATUS.eq(JStatusEnum.PASSED), 1).otherwise(0)).as("passed"),
				sum(when(tr.STATUS.eq(JStatusEnum.FAILED), 1).otherwise(0)).as("failed"),
				sum(when(tr.STATUS.eq(JStatusEnum.SKIPPED), 1).otherwise(0)).as("skipped"), count(tr.STATUS).as("total")
		)
				.from(ti).join(tr).on(ti.ITEM_ID.eq(tr.ITEM_ID))
				.join(l)
				.on(l.ID.eq(ti.LAUNCH_ID))
				.groupBy(l.ID, l.PROJECT_ID, l.USER_ID, l.NAME, l.DESCRIPTION, l.START_TIME, l.NUMBER, l.LAST_MODIFIED, l.MODE)
				.fetch(r -> new LaunchFull(r.into(com.epam.ta.reportportal.store.database.entity.launch.Launch.class),
						r.into(ExecutionStatistics.class)
				));
	}

	@Override
	public Long calculateApproximateDuration(Long launchId) {
		return null;
	}
}
