/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.PathProvider;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import java.security.Principal;

import static com.google.common.base.Predicates.or;
import static com.google.common.base.Predicates.not;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

/**
 * SWAGGER 2.0 UI page configuration for Report Portal application
 *
 * @author dzmitry_kavalets
 * @author Andrei_Ramanchuk
 * @author Andrei Varabyeu
 */
@Configuration
@Conditional(Conditions.NotTestCondition.class)
@EnableSwagger2
@ComponentScan(basePackages = "com.epam.ta.reportportal.ws.controller")
public class Swagger2Configuration {

	@Autowired
	private ServletContext servletContext;

	@Autowired
	@Value("${spring.application.name}")
	private String eurekaName;

	@Autowired
	@Value("${info.build.version}")
	private String buildVersion;

	@Bean
	public Docket docket() {
		/* For more information see default params at {@link ApiInfo} */
		ApiInfo rpInfo = new ApiInfo("Report Portal", "Report Portal API documentation", buildVersion, "urn:tos", "EPAM Systems",
				"GPLv3", "https://www.gnu.org/licenses/licenses.html#GPL");

		// @formatter:off
		Docket rpDocket = new Docket(DocumentationType.SWAGGER_2)
				.ignoredParameterTypes(Principal.class)
				.pathProvider(rpPathProvider())
				.useDefaultResponseMessages(false)
				/* remove default endpoints from listing */
				.select().apis(not(or(
							basePackage("org.springframework.boot"),
							basePackage("org.springframework.cloud"))))
				.build();
		//@formatter:on

		rpDocket.apiInfo(rpInfo);
		return rpDocket;
	}

	@Bean
	public PathProvider rpPathProvider() {
		return new RPPathProvider(servletContext, eurekaName);
	}

	@Bean
	public UiConfiguration uiConfig() {
		return new UiConfiguration(null);
	}

	private static class RPPathProvider extends RelativePathProvider {

		private String gatewayPath;

		RPPathProvider(ServletContext servletContext, String gatewayPath) {
			super(servletContext);
			this.gatewayPath = gatewayPath;
		}

		@Override
		protected String applicationPath() {
			return "/" + gatewayPath + super.applicationPath();
		}
	}
}