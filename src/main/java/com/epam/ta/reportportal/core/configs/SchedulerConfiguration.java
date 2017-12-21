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

import com.epam.ta.reportportal.job.CleanLogsJob;
import com.epam.ta.reportportal.job.CleanScreenshotsJob;
import com.epam.ta.reportportal.job.InterruptBrokenLaunchesJob;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.quartz.*;

import javax.inject.Named;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(SchedulerConfiguration.QuartzProperties.class)
public class SchedulerConfiguration {

	@Autowired
	List<Trigger> listOfTrigger;

	@Autowired
	private QuartzProperties quartzProperties;

	@Autowired
	private AutowireCapableBeanFactory context;

	@Bean
	@Primary
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
		scheduler.setApplicationContextSchedulerContextKey("applicationContext");

		scheduler.setQuartzProperties(quartzProperties.getQuartz());
		scheduler.setAutoStartup(true);  // to not automatically start after startup
		scheduler.setWaitForJobsToCompleteOnShutdown(true);
		scheduler.setJobFactory(beanJobFactory());

		// Here we will set all the trigger beans we have defined.
		if (null != listOfTrigger && !listOfTrigger.isEmpty()) {
			scheduler.setTriggers(listOfTrigger.toArray(new Trigger[listOfTrigger.size()]));
		}

		return scheduler;
	}

	@Bean
	public SpringBeanJobFactory beanJobFactory() {
		return new SpringBeanJobFactory() {
			@Override
			protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
				final Object jobInstance = super.createJobInstance(bundle);
				context.autowireBean(jobInstance);
				return jobInstance;
			}
		};
	}

	@Bean
	public SimpleTriggerFactoryBean createCleanLogsTrigger(@Named("cleanLogsJob") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.clean.logs.cron}") String cleanLogsCron) {
		return createTrigger(jobDetail, Duration.parse(cleanLogsCron).toMillis());
	}

	@Bean
	public SimpleTriggerFactoryBean interruptLaunchesTrigger(@Autowired @Named("interruptLaunchesJob") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.interrupt.broken.launches.cron}") String interruptLaunchesCron) {
		return createTrigger(jobDetail, Duration.parse(interruptLaunchesCron).toMillis());
	}

	@Bean
	public SimpleTriggerFactoryBean cleanScreenshotsTrigger(@Named("cleanScreenshotsJob") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.clean.screenshots.cron}") String cleanScreenshotsCron) {
		return createTrigger(jobDetail, Duration.parse(cleanScreenshotsCron).toMillis());
	}

	@Bean
	@Named("cleanLogsJob")
	public static JobDetailFactoryBean cleanLogsJob() {
		return createJobDetail(CleanLogsJob.class);
	}

	@Bean
	@Named("interruptLaunchesJob")
	public static JobDetailFactoryBean interruptLaunchesJob() {
		return createJobDetail(InterruptBrokenLaunchesJob.class);
	}

	@Bean
	@Named("cleanScreenshotsJob")
	public static JobDetailFactoryBean cleanScreenshotsJob() {
		return createJobDetail(CleanScreenshotsJob.class);
	}

	public static SimpleTriggerFactoryBean createTrigger(JobDetail jobDetail, long pollFrequencyMs) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(jobDetail);
		factoryBean.setStartDelay(0L);
		factoryBean.setRepeatInterval(pollFrequencyMs);
		factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		// in case of misfire, ignore all missed triggers and continue :
		factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
		return factoryBean;
	}

	// Use this method for creating cron triggers instead of simple triggers:
	public static CronTriggerFactoryBean createCronTrigger(JobDetail jobDetail, String cronExpression) {
		CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
		factoryBean.setJobDetail(jobDetail);
		factoryBean.setCronExpression(cronExpression);
		factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
		return factoryBean;
	}

	public static JobDetailFactoryBean createJobDetail(Class jobClass) {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(jobClass);
		// job has to be durable to be stored in DB:
		factoryBean.setDurability(true);
		return factoryBean;
	}

	@ConfigurationProperties("spring")
	public class QuartzProperties {

		private final Properties quartz = new Properties();

		public Properties getQuartz() {
			return quartz;
		}

	}

}
