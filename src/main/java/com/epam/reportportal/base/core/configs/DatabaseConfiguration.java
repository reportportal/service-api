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

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.FormatMapper;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.jpa.extensions.DefaultAnnotatedPojoMemberProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * JPA repositories, auditing, and transaction management.
 *
 * @author Pavel Bortnik
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = {
        "com.epam.reportportal.base.**.dao",
        "com.epam.reportportal.base.**.repository"
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
  public EntityManagerFactory entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(false);
    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("com.epam.reportportal.base.**.entity",
        "com.epam.reportportal.base.infrastructure.persistence.commons");
    factory.setDataSource(dataSource);
    factory.setJpaPropertyMap(Map.of(AvailableSettings.JSON_FORMAT_MAPPER, lenientJsonFormatMapper()));

    factory.afterPropertiesSet();

    return factory.getObject();
  }

  /**
   * Hibernate JSON mapper for jsonb columns. The entity factory is built by hand, so {@code spring.jpa.properties.*}
   * do not reach Hibernate and it would fall back to its strict default mapper. Unknown JSON fields must be ignored,
   * as the removed {@code JsonbUserType} did: existing rows contain extra keys written by it (for example
   * {@code comparable} or {@code defaultSqlLength} in {@code users.metadata}).
   */
  private static FormatMapper lenientJsonFormatMapper() {
    JsonMapper jsonMapper = JsonMapper.builderWithJackson2Defaults()
        .findAndAddModules()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
    return new Jackson3JsonFormatMapper(jsonMapper);
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

  /**
   * jOOQ 3.20+ requires an explicit {@code AnnotatedPojoMemberProvider} to map records into JPA-annotated POJOs. The
   * SPI is flagged {@code @ApiStatus.Internal}, but the official {@code jooq-jpa-extensions} module is the supported
   * way to provide it.
   */
  @Bean
  public DefaultConfiguration configuration() {
    DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
    jooqConfiguration.set(SQLDialect.POSTGRES);
    jooqConfiguration.set(new DefaultAnnotatedPojoMemberProvider());
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
