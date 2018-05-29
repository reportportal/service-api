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
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.epam.ta.reportportal.store.jooq.tables.JLog;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.epam.ta.reportportal.store.jooq.Tables.LOG;
import static com.epam.ta.reportportal.store.jooq.Tables.TEST_ITEM;

/**
 * @author Pavel Bortnik
 */
@Repository
public class LogRepositoryCustomImpl implements LogRepositoryCustom {

	private static final RecordMapper<? super Record, Log> LOG_MAPPER = r -> new Log(
			r.get(JLog.LOG.ID, Long.class),
			r.get(JLog.LOG.LOG_TIME, LocalDateTime.class),
			r.get(JLog.LOG.LOG_MESSAGE, String.class),
			r.get(JLog.LOG.LAST_MODIFIED, LocalDateTime.class),
			r.get(JLog.LOG.LOG_LEVEL, Integer.class),
			r.into(TestItem.class),
			r.get(JLog.LOG.FILE_PATH, String.class),
			r.get(JLog.LOG.THUMBNAIL_FILE_PATH, String.class),
			r.get(JLog.LOG.CONTENT_TYPE, String.class)
	);

	private DSLContext dsl;

	@Autowired
	public void setDsl(DSLContext dsl) {
		this.dsl = dsl;
	}

	@Override
	public boolean hasLogs(Long itemId) {
		return dsl.fetchExists(dsl.selectOne().from(LOG).where(LOG.ITEM_ID.eq(itemId)));
	}

	@Override
	public List<Log> findByTestItemId(String itemId, int limit, boolean isLoadBinaryData) {
		if (itemId == null || limit <= 0) {
			return new ArrayList<>();
		}

		Long id = Long.valueOf(itemId);

		return dsl.select()
				.from(LOG)
				.where(TEST_ITEM.ITEM_ID.eq(id))
				.orderBy(LOG.LOG_TIME.asc())
				.limit(limit)
				.fetch()
				.map(LOG_MAPPER);
	}

	@Override
	public List<Log> findByFilter(Filter filter) {

		return dsl.fetch(QueryBuilder.newBuilder(filter).build()).map(LOG_MAPPER);
	}
}
