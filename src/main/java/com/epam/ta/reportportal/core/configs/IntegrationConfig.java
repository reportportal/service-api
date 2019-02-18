/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.integration.util.EmailServerIntegrationService;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.core.integration.util.JiraIntegrationService;
import com.epam.ta.reportportal.core.integration.util.RallyIntegrationService;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class IntegrationConfig implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean
	public Map<ReportPortalIntegrationEnum, IntegrationService> integrationServiceMapping() {
		return ImmutableMap.<ReportPortalIntegrationEnum, IntegrationService>builder().put(ReportPortalIntegrationEnum.JIRA,
				applicationContext.getBean(JiraIntegrationService.class)
		)
				.put(ReportPortalIntegrationEnum.RALLY, applicationContext.getBean(RallyIntegrationService.class))
				.put(ReportPortalIntegrationEnum.EMAIL, applicationContext.getBean(EmailServerIntegrationService.class))
				.build();

	}
}
