/*
 * Copyright 2016 EPAM Systems
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
package com.epam.ta.reportportal.core.configs;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;

/**
 * Configuration conditions
 *
 * @author Andrei Varabyeu
 */
class Conditions {

	public static class NotTestCondition implements ConfigurationCondition {

		@Override
		public ConfigurationCondition.ConfigurationPhase getConfigurationPhase() {
			return ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION;
		}

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return Arrays.stream(context.getEnvironment().getActiveProfiles()).noneMatch(profile -> profile.equals("unittest"));
		}
	}

}