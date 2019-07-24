/*
 * Copyright 2019 EPAM Systems
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




