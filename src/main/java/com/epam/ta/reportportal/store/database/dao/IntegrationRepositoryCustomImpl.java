package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.commons.querygen.Filter;
import com.epam.ta.reportportal.store.commons.querygen.QueryBuilder;
import com.epam.ta.reportportal.store.database.entity.integration.Integration;
import com.epam.ta.reportportal.store.database.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.store.database.entity.integration.IntegrationType;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.jooq.tables.JIntegration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class IntegrationRepositoryCustomImpl implements IntegrationRepositoryCustom {

	private static final RecordMapper<? super Record, Integration> INTEGRATION_MAPPER = r -> new Integration(
			r.get(JIntegration.INTEGRATION.ID, Long.class),
			r.into(Project.class),
			r.get(JIntegration.INTEGRATION.TYPE, IntegrationType.class),
			r.get(JIntegration.INTEGRATION.PARAMS, IntegrationParams.class),
			r.get(JIntegration.INTEGRATION.CREATION_DATE, LocalDateTime.class)
	);

	@Autowired
	private DSLContext dsl;

	@Override
	public List<Integration> findByFilter(Filter filter) {

		return dsl.fetch(QueryBuilder.newBuilder(filter).build()).map(INTEGRATION_MAPPER);
	}
}
