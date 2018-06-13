package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.jooq.tables.JLaunch;
import com.epam.ta.reportportal.store.jooq.tables.JLaunchTag;
import com.epam.ta.reportportal.store.jooq.tables.JProject;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.epam.ta.reportportal.store.jooq.Tables.LAUNCH;
import static com.epam.ta.reportportal.store.jooq.Tables.LAUNCH_TAG;
import static com.epam.ta.reportportal.store.jooq.Tables.PROJECT;

/**
 * @author Yauheni_Martynau
 */
@Repository
public class LaunchTagRepositoryCustomImpl implements LaunchTagRepositoryCustom {

	private final DSLContext dslContext;

	public LaunchTagRepositoryCustomImpl(DSLContext dslContext) {

		this.dslContext = dslContext;
	}

	@Override
	public List<String> getTags(String projectName, String value) {

		JLaunch l = LAUNCH.as("l");
		JProject p = PROJECT.as("p");
		JLaunchTag lt = LAUNCH_TAG.as("lt");

		return dslContext.select()
				.from(lt)
				.leftJoin(l).on(lt.LAUNCH_ID.eq(l.ID))
				.leftJoin(p).on(l.PROJECT_ID.eq(p.ID))
				.where(p.NAME.like("%" + value + "%"))
				.fetch(LAUNCH_TAG.VALUE);
	}
}
