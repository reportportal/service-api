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

package com.epam.ta.reportportal.core.configs;

import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.reportportal.commons.template.TemplateEngineProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Global Email Configuration<br>
 * Probably will be replaces by configuration per project
 *
 * @author Andrei_Ramanchuk
 */
@Configuration
public class EmailConfiguration {

	@Bean
	public ThreadPoolTaskExecutor emailExecutorService() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(5);
		threadPoolTaskExecutor.setMaxPoolSize(20);
		threadPoolTaskExecutor.setQueueCapacity(50);
		threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
		threadPoolTaskExecutor.setAwaitTerminationSeconds(20);
		threadPoolTaskExecutor.setThreadNamePrefix("email-sending-exec");
		return threadPoolTaskExecutor;
	}

	@Bean
	@Primary
	public TemplateEngine getTemplateEngine() {
		return new TemplateEngineProvider("/templates/email").get();
	}

}
