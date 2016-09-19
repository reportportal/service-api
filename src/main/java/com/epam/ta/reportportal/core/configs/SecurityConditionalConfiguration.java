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

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Conditional EPAM specific spring security configuration
 * 
 * @author Andrei_Ramanchuk
 */
@Configuration
@Conditional(SecurityConditionalConfiguration.Condition.class)
@ImportResource("classpath:/spring-security-epam.xml")
public class SecurityConditionalConfiguration {
	static class Condition implements ConfigurationCondition {
		@Override
		public ConfigurationPhase getConfigurationPhase() {
			return ConfigurationPhase.PARSE_CONFIGURATION;
		}

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return System.getProperty("spring.profiles.active") != null && System.getProperty("spring.profiles.active").contains("epam");
		}
	}
}