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

package com.epam.ta.reportportal.store.config;

import com.epam.ta.reportportal.store.database.dao.ReportPortalRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Pavel Bortnik
 */

@EnableJpaAuditing
@EntityScan(basePackages = "com.epam.ta.reportportal.store")
@EnableJpaRepositories(basePackages = { "com.epam.ta.reportportal.store" }, repositoryBaseClass = ReportPortalRepositoryImpl.class)
public class DatabaseConfiguration {

	//	@Autowired
	//	private DataSourceProperties properties;
	//
	//	@Bean
	//	public DataSource dataSource() {
	//		return properties.initializeDataSourceBuilder().build();
	//	}
	//
	//	@Bean
	//	public EntityManagerFactory entityManagerFactory() {
	//
	//		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
	//		vendorAdapter.setGenerateDdl(true);
	//
	//		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
	//		factory.setJpaVendorAdapter(vendorAdapter);
	//		factory.setPackagesToScan("com.epam.ta.reportportal.database.entity");
	//		factory.setDataSource(dataSource());
	//		factory.afterPropertiesSet();
	//
	//		return factory.getObject();
	//	}
	//
	//	@Bean
	//	public PlatformTransactionManager transactionManager() {
	//		JpaTransactionManager txManager = new JpaTransactionManager();
	//		txManager.setEntityManagerFactory(entityManagerFactory());
	//		txManager.setDataSource(dataSource());
	//		return txManager;
	//	}
	//
	//	@Bean
	//	public TransactionAwareDataSourceProxy transactionAwareDataSource() {
	//		return new TransactionAwareDataSourceProxy(dataSource());
	//	}
	//
	//	@Bean
	//	public DataSourceConnectionProvider connectionProvider() {
	//		return new DataSourceConnectionProvider(transactionAwareDataSource());
	//	}
	//
	//	@Bean
	//	public DefaultConfiguration configuration() {
	//		DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
	//		jooqConfiguration.set(SQLDialect.POSTGRES);
	//		jooqConfiguration.setConnectionProvider(connectionProvider());
	//		return jooqConfiguration;
	//	}
	//
	//	@Bean
	//	public DefaultDSLContext dsl() {
	//		return new DefaultDSLContext(configuration());
	//	}
}
