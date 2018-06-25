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

import com.epam.ta.reportportal.store.commons.querygen.Filter;
import com.epam.ta.reportportal.store.commons.querygen.QueryBuilder;
import com.epam.ta.reportportal.store.database.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.launch.ExecutionStatistics;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.launch.LaunchFull;
import com.epam.ta.reportportal.store.jooq.enums.JLaunchModeEnum;
import com.epam.ta.reportportal.store.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.store.jooq.enums.JTestItemTypeEnum;
import com.epam.ta.reportportal.store.jooq.tables.*;
import com.google.common.collect.Lists;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.store.jooq.Tables.*;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.when;

/**
 * @author Pavel Bortnik
 */
@Repository
public class LaunchRepositoryCustomImpl implements LaunchRepositoryCustom {

	private static final RecordMapper<? super Record, LaunchFull> LAUNCH_FULL_MAPPER = r -> new LaunchFull(r.into(Launch.class),
			r.into(ExecutionStatistics.class)
	);

	private static final RecordMapper<? super Record, Launch> LAUNCH_MAPPER = r -> new Launch(r.get(JLaunch.LAUNCH.ID, Long.class),
			r.get(JLaunch.LAUNCH.UUID, String.class), r.get(JLaunch.LAUNCH.PROJECT_ID, Long.class),
			r.get(JLaunch.LAUNCH.USER_ID, Long.class), r.get(JLaunch.LAUNCH.NAME, String.class),
			r.get(JLaunch.LAUNCH.DESCRIPTION, String.class), r.get(JLaunch.LAUNCH.START_TIME, LocalDateTime.class),
			r.get(JLaunch.LAUNCH.END_TIME, LocalDateTime.class), r.get(JLaunch.LAUNCH.NUMBER, Long.class),
			r.get(JLaunch.LAUNCH.LAST_MODIFIED, LocalDateTime.class), r.get(JLaunch.LAUNCH.MODE, LaunchModeEnum.class),
			r.get(JLaunch.LAUNCH.STATUS, StatusEnum.class)
	);

	@Autowired
	private DSLContext dsl;

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
				sum(when(tr.STATUS.eq(JStatusEnum.SKIPPED), 1).otherwise(0)).as("skipped"), DSL.count(tr.STATUS).as("total")
		)
				.from(l)
				.leftJoin(ti).on(l.ID.eq(ti.LAUNCH_ID)).leftJoin(tr).on(ti.ITEM_ID.eq(tr.ITEM_ID)).where(ti.TYPE.eq(JTestItemTypeEnum.STEP))
				.groupBy(l.ID, l.PROJECT_ID, l.USER_ID, l.NAME, l.DESCRIPTION, l.START_TIME, l.NUMBER, l.LAST_MODIFIED, l.MODE)
				.fetch(LAUNCH_FULL_MAPPER);
	}

	public List<LaunchFull> findByFilter(Filter filter) {
		return dsl.fetch(QueryBuilder.newBuilder(filter).build()).map(LAUNCH_FULL_MAPPER);
	}

	public Page<LaunchFull> findByFilter(Filter filter, Pageable pageable) {
		return PageableExecutionUtils.getPage(dsl.fetch(QueryBuilder.newBuilder(filter).with(pageable).build()).map(LAUNCH_FULL_MAPPER),
				pageable, () -> dsl.fetchCount(QueryBuilder.newBuilder(filter).build())
		);
	}

	@Override
	public Page<LaunchFull> findLatest(Filter filter, Pageable pageable) {

		List<LaunchFull> launches = dsl.fetch(QueryBuilder.newBuilder(filter).with(pageable).build()).map(LAUNCH_FULL_MAPPER);

		return PageableExecutionUtils.getPage(getLatest(launches), pageable, () -> dsl.fetchCount(QueryBuilder.newBuilder(filter).build()));
	}

	@Override
	public List<String> getLaunchNames(Long projectId, String value, LaunchModeEnum mode) {

		JLaunch l = LAUNCH.as("l");
		JProject p = PROJECT.as("p");

		return dsl.select().from(l).leftJoin(p).on(l.PROJECT_ID.eq(p.ID)).where(p.ID.eq(projectId)).and(l.NAME.like(value)).fetch(l.NAME);
	}

	@Override
	public List<String> getOwnerNames(Long projectId, String value, String mode) {

		JLaunch l = LAUNCH.as("l");
		JProject p = PROJECT.as("p");
		JUsers u = USERS.as("u");

		return dsl.selectDistinct()
				.from(l).leftJoin(p).on(l.PROJECT_ID.eq(p.ID)).leftJoin(u).on(l.USER_ID.eq(u.ID))
				.where(p.ID.eq(projectId))
				.and(u.FULL_NAME.like("%" + value + "%"))
				.and(l.MODE.eq(JLaunchModeEnum.valueOf(mode)))
				.fetch(u.FULL_NAME);
	}

	@Override
	public Map<String, String> getStatuses(Long projectId, Long[] ids) {

		JLaunch l = LAUNCH.as("l");
		JProject p = PROJECT.as("p");

		return dsl.select()
				.from(l)
				.leftJoin(p)
				.on(l.PROJECT_ID.eq(p.ID))
				.where(p.ID.eq(projectId))
				.and(l.ID.in(ids))
				.fetch(LAUNCH_MAPPER)
				.stream().collect(Collectors.toMap(launch -> String.valueOf(launch.getId()), launch -> launch.getStatus().toString()));
	}

	@Override
	public Launch findLatestByName(String launchName) {
		return dsl.select()
				.distinctOn(LAUNCH.NAME)
				.from(LAUNCH)
				.where(LAUNCH.NAME.eq(launchName))
				.orderBy(LAUNCH.NAME, LAUNCH.NUMBER.desc())
				.fetchOne()
				.into(Launch.class);
	}

	private List<LaunchFull> getLatest(List<LaunchFull> fullLaunches) {

		List<LaunchFull> latestLaunches = Lists.newArrayList();

		fullLaunches.forEach(full -> {
			AtomicBoolean added = new AtomicBoolean(false);

			latestLaunches.forEach(latest -> {
				if (latest.getLaunch().getName().equals(full.getLaunch().getName())) {

					if (latest.getLaunch().getNumber() < full.getLaunch().getNumber()) {
						latestLaunches.set(latestLaunches.indexOf(latest), full);
						added.set(true);
					}
				}
			});

			if (!added.get()) {
				latestLaunches.add(full);
			}
		});

		return latestLaunches;
	}
}
