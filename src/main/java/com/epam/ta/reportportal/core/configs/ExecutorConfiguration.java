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

import com.epam.ta.reportportal.core.log.impl.SaveLogBinaryDataTask;
import com.epam.ta.reportportal.core.log.impl.SaveLogBinaryDataTaskAsync;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configs for beans related to job execution
 *
 * @author Andrei Varabyeu
 */
@Configuration
@EnableAsync
public class ExecutorConfiguration {

	@Bean
	@Primary
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(5);
		scheduler.setThreadNamePrefix("default-task-sched");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		return scheduler;
	}

	@Bean(name = "saveLogsTaskExecutor")
	public TaskExecutor saveLogsTaskExecutor(@Value("${rp.environment.variable.executor.pool.save-logs.core}") Integer corePoolSize,
			@Value("${rp.environment.variable.executor.pool.save-logs.max}") Integer maxPoolSize,
			@Value("${rp.environment.variable.executor.pool.save-logs.queue}") Integer queueCapacity) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setThreadNamePrefix("logs-task-exec");
		executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
		return executor;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SaveLogBinaryDataTask saveLogBinaryDataTask() {
		return new SaveLogBinaryDataTask();
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SaveLogBinaryDataTaskAsync saveLogBinaryDataTaskAsync() {
		return new SaveLogBinaryDataTaskAsync();
	}

	@EnableScheduling
	public static class SchedulingConfiguration {
	}

	@Bean(name = "autoAnalyzeTaskExecutor")
	public TaskExecutor autoAnalyzeTaskExecutor(@Value("${rp.environment.variable.executor.pool.auto-analyze.core}") Integer corePoolSize,
			@Value("${rp.environment.variable.executor.pool.auto-analyze.max}") Integer maxPoolSize,
			@Value("${rp.environment.variable.executor.pool.auto-analyze.queue}") Integer queueCapacity) {
		final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
		threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
		threadPoolTaskExecutor.setThreadNamePrefix("auto-analyze-exec");
		return threadPoolTaskExecutor;
	}

	@Bean(name = "demoDataTaskExecutor")
	public TaskExecutor demoDataTaskExecutor(@Value("${rp.environment.variable.executor.pool.demo-data.core}") Integer corePoolSize,
			@Value("${rp.environment.variable.executor.pool.demo-data.max}") Integer maxPoolSize,
			@Value("${rp.environment.variable.executor.pool.demo-data.queue}") Integer queueCapacity) {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
		threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
		threadPoolTaskExecutor.setAwaitTerminationSeconds(60);
		threadPoolTaskExecutor.setThreadNamePrefix("demo-data-exec");
		return threadPoolTaskExecutor;
	}

	@Bean(name = "widgetViewExecutor")
	public TaskExecutor healthCheckTableExecutor(
			@Value("${rp.environment.variable.executor.pool.widget-view.core}") Integer corePoolSize,
			@Value("${rp.environment.variable.executor.pool.widget-view.max}") Integer maxPoolSize,
			@Value("${rp.environment.variable.executor.pool.widget-view.queue}") Integer queueCapacity) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setThreadNamePrefix("generate-widget-view-task");
		executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
		return executor;
	}

}
