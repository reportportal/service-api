/*
 * Copyright 2017 EPAM Systems
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

import com.epam.ta.reportportal.job.SaveBinaryDataJob;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configs for beans related to job execution
 *
 * @author Andrei Varabyeu
 */
@Configuration
@ComponentScan("com.epam.ta.reportportal.job")
public class JobsConfiguration {

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(5);
		scheduler.setThreadNamePrefix("default-task-sched");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		return scheduler;
	}

	@Bean(name = "saveLogsTaskExecutor")
	public TaskExecutor saveLogsTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(100);
		executor.setQueueCapacity(400);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setThreadNamePrefix("logs-task-exec");
		executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
		return executor;
	}

	@Bean(name = "saveBinaryDataJob")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SaveBinaryDataJob saveBinaryDataJob() {
		return new SaveBinaryDataJob();
	}

	@EnableScheduling
	public static class SchedulingConfiguration {
	}

	@Bean(name = "autoAnalyzeTaskExecutor")
	public TaskExecutor autoAnalyzeTaskExecutor() {
		final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(10);
		threadPoolTaskExecutor.setMaxPoolSize(30);
		threadPoolTaskExecutor.setQueueCapacity(200);
		threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
		threadPoolTaskExecutor.setThreadNamePrefix("auto-analyze-exec");
		return threadPoolTaskExecutor;
	}

}