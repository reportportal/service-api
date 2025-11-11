/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.core.configs;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.Serializable;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

/**
 * @author Pavel Bortnik
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = {
        "com.epam.reportportal.**.dao",
        "com.epam.reportportal.**.repository"
    },
    repositoryBaseClass = ReportPortalRepositoryImpl.class,
    repositoryFactoryBeanClass = DatabaseConfiguration.RpRepoFactoryBean.class
)
@EnableTransactionManagement
@EnableCaching
public class DatabaseConfiguration {

  @Autowired
  private DataSource dataSource;

  @Bean
  @Profile("unittest")
  public Flyway flyway() throws IOException {
    return Flyway.configure().dataSource(dataSource).validateOnMigrate(false).load();
  }

  @Bean
  public EntityManagerFactory entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(false);
    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("com.epam.reportportal.infrastructure.persistence.commons",
        "com.epam.reportportal.**.entity");
    factory.setDataSource(dataSource);

    factory.afterPropertiesSet();

    return factory.getObject();
  }

  @Bean
  @Primary
  public PlatformTransactionManager transactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory());
    return transactionManager;
  }

  @Bean
  public TransactionAwareDataSourceProxy transactionAwareDataSource() {
    return new TransactionAwareDataSourceProxy(dataSource);
  }

  @Bean
  public DataSourceConnectionProvider connectionProvider() {
    return new DataSourceConnectionProvider(transactionAwareDataSource());
  }

  @Bean
  public DefaultConfiguration configuration() {
    DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
    jooqConfiguration.set(SQLDialect.POSTGRES);
    jooqConfiguration.setConnectionProvider(connectionProvider());
    return jooqConfiguration;
  }

  @Bean
  public DefaultDSLContext dsl() {
    return new DefaultDSLContext(configuration());
  }

  public static class RpRepoFactoryBean<T extends Repository<S, ID>, S, ID> extends
      JpaRepositoryFactoryBean {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public RpRepoFactoryBean(Class repositoryInterface) {
      super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
      return new JpaRepositoryFactory(entityManager) {
        @Override
        public <T> T getRepository(Class<T> repositoryInterface) {
          T repo = super.getRepository(repositoryInterface);
          beanFactory.autowireBean(repo);
          return repo;
        }

        @Override
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(
            RepositoryInformation information, EntityManager em) {

          JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(
              information.getDomainType());
          Object repository = getTargetRepositoryViaReflection(information, entityInformation, em);

          Assert.isInstanceOf(JpaRepositoryImplementation.class, repository);
          beanFactory.autowireBean(repository);
          return (JpaRepositoryImplementation<?, ?>) repository;
        }
      };
    }
  }
}
