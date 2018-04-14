package com.epam.ta.reportportal.store.commons.querygen;

import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.store.jooq.tables.JLaunch;
import com.epam.ta.reportportal.store.jooq.tables.JProject;
import com.epam.ta.reportportal.store.jooq.tables.JTestItem;
import com.epam.ta.reportportal.store.jooq.tables.JTestItemResults;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.store.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

public enum FilterTarget {

	LAUNCH(Launch.class, Arrays.asList(
			//@formatter:off
			new CriteriaHolder("description", "l.description", String.class, false),
			new CriteriaHolder("name", "l.name", String.class, false),
			new CriteriaHolder("project", "p.name", String.class, false)
			//@formatter:on
	)) {
		public SelectQuery<? extends Record> getQuery() {
			JLaunch l = JLaunch.LAUNCH.as("l");
			JTestItem ti = TEST_ITEM.as("ti");
			JProject p = PROJECT.as("p");
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
					//@formatter:off
					.from(l)
					.leftJoin(ti).on(l.ID.eq(ti.LAUNCH_ID))
					.leftJoin(tr).on(ti.ITEM_ID.eq(tr.ITEM_ID))
					.leftJoin(p).on(l.PROJECT_ID.eq(p.ID))
					.groupBy(l.ID, l.PROJECT_ID, l.USER_ID, l.NAME, l.DESCRIPTION, l.START_TIME, l.NUMBER, l.LAST_MODIFIED, l.MODE)
					.getQuery();
					//@formatter:on
		}
	};

	private Class<?> clazz;
	private List<CriteriaHolder> criterias;

	FilterTarget(Class<?> clazz, List<CriteriaHolder> criterias) {
		this.clazz = clazz;
		this.criterias = criterias;
	}

	public abstract SelectQuery<? extends Record> getQuery();

	public Class<?> getClazz() {
		return clazz;
	}

	public List<CriteriaHolder> getCriterias() {
		return criterias;
	}

	public Optional<CriteriaHolder> getCriteriaByFilter(String filterCriteria) {
		return criterias.stream().filter(holder -> holder.getFilterCriteria().equals(filterCriteria)).findAny();
	}

	public static FilterTarget findByClass(Class<?> clazz) {
		return Arrays.stream(values())
				.filter(val -> val.clazz.equals(clazz))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException(String.format("No target query builder for clazz %s", clazz)));
	}

}
