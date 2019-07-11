package com.epam.ta.reportportal.generator;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for generating JOOQ entities
 *
 * @author Yauheni_Martynau
 */
@Disabled
@TestPropertySource("classpath:test-application.properties")
class JooqGenerator {

	@Value("${spring.datasource.driver-class-name}")
	private String driver;

	@Value("${generator.datasource.url}")
	private String url;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${generator.default.generator.name}")
	private String defaultGeneratorName;

	@Value("${generator.package.name}")
	private String packageName;

	@Value("${generator.directory}")
	private String directory;

	@Value("${generator.schema}")
	private String schema;

	@Test
	void generate() {

		Configuration configuration = new Configuration().withJdbc(new Jdbc().withDriver(driver)
				.withUrl(url)
				.withUser(username)
				.withPassword(password))
				.withGenerator(new Generator().withStrategy(new Strategy().withName(PrefixGeneratorStrategy.class.getName()))
						.withName(defaultGeneratorName)
						.withDatabase(new Database().withIncludes(".*").withSchemata(new Schema().withInputSchema(schema)))
						.withTarget(new Target().withPackageName(packageName).withDirectory(directory)));
		try {
			GenerationTool.generate(configuration);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}




