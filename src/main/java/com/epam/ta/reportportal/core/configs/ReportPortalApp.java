/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.configs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Application Entry Point
 *
 * @author Andrei Varabyeu
 */
@SpringBootApplication(scanBasePackages = { "com.epam.ta.reportportal", "com.epam.reportportal" }, exclude = {
		MultipartAutoConfiguration.class, FlywayAutoConfiguration.class })
@Configuration
@Import({ com.epam.ta.reportportal.config.DatabaseConfiguration.class })
public class ReportPortalApp {

	public static void main(String[] args) {
		SpringApplication.run(ReportPortalApp.class, args);
	}

}
