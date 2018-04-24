package com.epam.ta.reportportal.store;

import com.epam.ta.reportportal.store.config.DatabaseConfiguration;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;

@Configuration
@EnableConfigurationProperties
@EnableAutoConfiguration
@Import(DatabaseConfiguration.class)
@PropertySource("classpath:test-application.properties")
public class InMemoryDbConfig {

	@Autowired
	private DataSource dataSource;

	@Bean
	public DatabaseConnection dbunitConnection() throws SQLException, DatabaseUnitException {
		DatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
		connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory() {
			@Override
			public boolean isEnumType(String sqlTypeName) {
				return sqlTypeName.endsWith("enum");
			}

			@Override
			public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
				if (isEnumType(sqlTypeName)) {
					sqlType = Types.OTHER;
				}
				return super.createDataType(sqlType, sqlTypeName);
			}
		});
		return connection;
	}

}
