package com.epam.ta.reportportal.store.commons;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

/**
 * Postgres Dialect aware of JSON/JSONB types
 *
 * @author Andrei Varabyeu
 */
public class JsonbAwarePostgresDialect extends PostgreSQL95Dialect {

	public JsonbAwarePostgresDialect() {
		super();
		this.registerColumnType(Types.JAVA_OBJECT, "json");
	}
}
