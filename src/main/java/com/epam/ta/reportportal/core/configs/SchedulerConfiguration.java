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

import com.epam.reportportal.extension.classloader.ReportPortalResourceLoader;
import com.epam.ta.reportportal.job.*;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.quartz.*;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;
import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Configuration
@Conditional(Conditions.NotTestCondition.class)
@EnableConfigurationProperties({ SchedulerConfiguration.QuartzProperties.class })
public class SchedulerConfiguration {

	@Autowired
	List<Trigger> listOfTrigger;

	@Autowired
	private QuartzProperties quartzProperties;

	@Autowired
	private AutowireCapableBeanFactory context;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ReportPortalResourceLoader resourceLoader;

	@Bean
	@Primary
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean scheduler = new SchedulerFactoryBean() {
			@Override
			public void setResourceLoader(ResourceLoader resourceLoader) {
				if (this.resourceLoader == null) {
					super.setResourceLoader(resourceLoader);
				}
			}
		};
		scheduler.setApplicationContextSchedulerContextKey("applicationContext");

		scheduler.setOverwriteExistingJobs(true);
		scheduler.setResourceLoader(resourceLoader);
		scheduler.setQuartzProperties(quartzProperties.getQuartz());
		scheduler.setDataSource(dataSource);
		scheduler.setTransactionManager(transactionManager);
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
			protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
				final Object jobInstance = super.createJobInstance(bundle);
				context.autowireBean(jobInstance);
				return jobInstance;
			}
		};
	}

	@Bean
	@Profile("!demo")
	public SimpleTriggerFactoryBean createCleanLogsTrigger(@Named("cleanLogsJobBean") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.clean.logs.cron}") String cleanLogsCron) {
		return createTriggerDelayed(jobDetail, Duration.parse(cleanLogsCron).toMillis());
	}

	@Bean
	@Profile("!demo")
	public SimpleTriggerFactoryBean cleanScreenshotsTrigger(@Named("cleanScreenshotsJobBean") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.clean.screenshots.cron}") String cleanScreenshotsCron) {
		return createTriggerDelayed(jobDetail, Duration.parse(cleanScreenshotsCron).toMillis());
	}

	@Bean
	@Profile("!demo")
	public SimpleTriggerFactoryBean createCleanLaunchesTrigger(@Named("cleanLaunchesJobBean") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.clean.launches.cron}") String cleanLaunchesCron) {
		return createTriggerDelayed(jobDetail, Duration.parse(cleanLaunchesCron).toMillis());
	}

	@Bean
	public SimpleTriggerFactoryBean interruptLaunchesTrigger(@Named("interruptLaunchesJobBean") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.interrupt.broken.launches.cron}") String interruptLaunchesCron) {
		return createTriggerDelayed(jobDetail, Duration.parse(interruptLaunchesCron).toMillis());
	}

	@Bean
	public SimpleTriggerFactoryBean cleanExpiredCreationBidsTrigger(@Named("cleanExpiredCreationBidsJobBean") JobDetail jobDetail,
			@Value("${com.ta.reportportal.job.clean.bids.cron}") String cleanBidsCron) {
		return createTrigger(jobDetail, Duration.parse(cleanBidsCron).toMillis());
	}

	@Bean
	@Profile("demo")
	public SimpleTriggerFactoryBean flushingDataTrigger(@Named("flushingDataJob") JobDetail jobDetail,
			@Value("${com.ta.reportportal.rp.flushing.time.cron}") String flushingCron) {
		return createTrigger(jobDetail, Duration.parse(flushingCron).toMillis());
	}

	@Bean("interruptLaunchesJobBean")
	public JobDetailFactoryBean interruptLaunchesJob() {
		return createJobDetail(InterruptBrokenLaunchesJob.class);
	}

	@Bean("cleanExpiredCreationBidsJobBean")
	public JobDetailFactoryBean cleanExpiredCreationBidsJob() {
		return createJobDetail(CleanExpiredCreationBidsJob.class);
	}

	@Bean("cleanLogsJobBean")
	@Profile("!demo")
	public JobDetailFactoryBean cleanLogsJob() {
		return createJobDetail(CleanLogsJob.class);
	}

	@Bean("cleanScreenshotsJobBean")
	@Profile("!demo")
	public JobDetailFactoryBean cleanScreenshotsJob() {
		return createJobDetail(CleanScreenshotsJob.class);
	}

	@Bean("cleanLaunchesJobBean")
	@Profile("!demo")
	public JobDetailFactoryBean cleanLaunchesJob() {
		return createJobDetail(CleanLaunchesJob.class);
	}

	@Bean
	@Profile("demo")
	@Named("flushingDataJob")
	public JobDetailFactoryBean flushingDataJob() {
		return createJobDetail(FlushingDataJob.class);
	}

	public SimpleTriggerFactoryBean createTrigger(JobDetail jobDetail, long pollFrequencyMs) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(jobDetail);
		factoryBean.setStartDelay(0L);
		factoryBean.setRepeatInterval(pollFrequencyMs);
		factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		// in case of misfire, ignore all missed triggers and continue :
		factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
		return factoryBean;
	}

	public SimpleTriggerFactoryBean createTriggerDelayed(JobDetail jobDetail, long pollFrequencyMs) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(jobDetail);
		factoryBean.setStartDelay(pollFrequencyMs);
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

	public static JobDetailFactoryBean createJobDetail(Class<? extends Job> jobClass) {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(jobClass);
		// job has to be durable to be stored in DB:
		factoryBean.setDurability(true);
		return factoryBean;
	}

	@ConfigurationProperties("spring.application")
	public static class QuartzProperties {

		private final Properties quartz = new Properties();

		public Properties getQuartz() {
			return quartz;
		}

	}

}
