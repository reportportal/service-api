package com.epam.ta.reportportal.store.commons.querygen;

import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.store.jooq.tables.JLaunch;
import com.epam.ta.reportportal.store.jooq.tables.JTestItem;
import com.epam.ta.reportportal.store.jooq.tables.JTestItemResults;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

import java.util.Arrays;
import java.util.Set;

import static com.epam.ta.reportportal.store.jooq.Tables.TEST_ITEM;
import static com.epam.ta.reportportal.store.jooq.Tables.TEST_ITEM_RESULTS;
import static org.jooq.impl.DSL.*;

public enum Target {

	LAUNCH(Launch.class) {
		public SelectQuery<? extends Record> getQuery() {
			JLaunch l = JLaunch.LAUNCH.as("l");
			JTestItem ti = TEST_ITEM.as("ti");
			JTestItemResults tr = TEST_ITEM_RESULTS.as("tr");

			return DSL.select(
					l.ID,
					l.PROJECT_ID,
					l.USER_ID,
					l.NAME,
					l.DESCRIPTION,
					l.START_TIME,
					l.NUMBER,
					l.LAST_MODIFIED,
					l.MODE,
					sum(when(tr.STATUS.eq(JStatusEnum.PASSED), 1).otherwise(0)).as("passed"),
					sum(when(tr.STATUS.eq(JStatusEnum.FAILED), 1).otherwise(0)).as("failed"),
					sum(when(tr.STATUS.eq(JStatusEnum.SKIPPED), 1).otherwise(0)).as("skipped"),
					count(tr.STATUS).as("total")
			)
					.from(ti)
					.join(tr)
					.on(ti.ITEM_ID.eq(tr.ITEM_ID))
					.join(l)
					.on(l.ID.eq(ti.LAUNCH_ID))
					.groupBy(l.ID, l.PROJECT_ID, l.USER_ID, l.NAME, l.DESCRIPTION, l.START_TIME, l.NUMBER, l.LAST_MODIFIED, l.MODE)
					.getQuery();
		}
	};

	private Class<?> clazz;

	Target(Class<?> clazz) {
		this.clazz = clazz;
	}

	public abstract SelectQuery<? extends Record> getQuery();

	public Class<?> getClazz() {
		return clazz;
	}

	public static Target findByClass(Class<?> clazz) {
		return Arrays.stream(values())
				.filter(val -> val.clazz.equals(clazz))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException(String.format("No target query builder for clazz %s", clazz)));
	}
}
