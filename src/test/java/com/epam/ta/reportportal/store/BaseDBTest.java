package com.epam.ta.reportportal.store;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = InMemoryDbConfig.class, initializers = BaseDBTest.Initializer.class)
@TestExecutionListeners(listeners = BaseDBTest.DatasetImportListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Ignore
public class BaseDBTest {

	@ClassRule
	public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer().withUsername("rpuser")
			.withPassword("rppass")
			.withDatabaseName("reportportal");

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of("spring.datasource.url=" + postgresContainer.getJdbcUrl()).applyTo(configurableApplicationContext);
		}
	}

	public static class DatasetImportListener implements TestExecutionListener {

		@Override
		public void prepareTestInstance(TestContext testContext) throws IOException, DatabaseUnitException, SQLException {
			if (testContext.getTestClass().isAnnotationPresent(ImportDataset.class)) {
				ImportDataset dataset = testContext.getTestClass().getAnnotation(ImportDataset.class);

				FlatXmlDataSet dataSet = new FlatXmlDataSet(new FlatXmlProducer(new InputSource(testContext.getApplicationContext()
						.getResource(dataset.value())
						.getInputStream())));
				DatabaseOperation.CLEAN_INSERT.execute(testContext.getApplicationContext().getBean(DatabaseConnection.class), dataSet);
			}
		}
	}

}
