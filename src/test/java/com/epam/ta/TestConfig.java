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

package com.epam.ta;

import com.epam.ta.reportportal.auth.UatClient;
import com.epam.ta.reportportal.database.fixture.MongoFixtureImporter;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.events.ConsulUpdateEvent;
import com.github.fakemongo.Fongo;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.mongodb.MockMongoClient;
import com.mongodb.WriteConcern;
import org.mockito.Mockito;
import org.quartz.*;
import org.quartz.Calendar;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.epam.ta.reportportal.config.CacheConfiguration.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@Configuration
@ImportResource({ "classpath:report-portal-ws-servlet.xml", "classpath:spring-demo-data.xml" })
@PropertySource("classpath:unittests.properties")
@EnableCaching(mode = AdviceMode.PROXY)
public class TestConfig {

	@Value("#{new Long(${rp.cache.project.size})}")
	private long projectCacheSize;

	@Value("#{new Long(${rp.cache.ticket.size})}")
	private long ticketCacheSize;

	@Value("#{new Long(${rp.cache.user.size})}")
	private long userCacheSize;

	@Value("#{new Long(${rp.cache.project.expiration})}")
	private long projectCacheExpiration;

	@Value("#{new Long(${rp.cache.ticket.expiration})}")
	private long ticketCacheExpiration;

	@Value("#{new Long(${rp.cache.user.expiration})}")
	private long userCacheExpiration;

	@Value("#{new Long(${rp.cache.project.info})}")
	private long projectInfoCacheExpiration;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	@Primary
	public MongoDbFactory mongoDbFactory() {
		final Fongo fongo = new Fongo("InMemoryMongo");
		SimpleMongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(MockMongoClient.create(fongo), "reportportal");
		mongoDbFactory.setWriteConcern(WriteConcern.ACKNOWLEDGED);
		return mongoDbFactory;
	}

	@Bean
	public SpringFixtureRule springFixtureRule() {
		return new SpringFixtureRule();
	}

	@Bean
	public UatClient uatClient() {
		return mock(UatClient.class);
	}

	@Bean
	public OAuth2ProtectedResourceDetails oauthResource() {
		return mock(OAuth2ProtectedResourceDetails.class);
	}

	@Bean
	public MongoFixtureImporter mongoFixtureImporter() {
		return new MongoFixtureImporter();
	}

	@Bean
	@Scope(scopeName = "prototype")
	public OAuth2ClientContext oAuth2ClientContext() {
		return new DefaultOAuth2ClientContext();
	}

	@Bean
	public ApplicationEventPublisher applicationEventPublisher() {
		ApplicationEventPublisher mock = spy(ApplicationEventPublisher.class);
		doNothing().when(mock).publishEvent(any(ConsulUpdateEvent.class));
		return mock;
	}

	@Bean
	@Primary
	public CacheManager getGlobalCacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();

		GuavaCache tickets = new GuavaCache(EXTERNAL_SYSTEM_TICKET_CACHE,
				CacheBuilder.newBuilder()
						.maximumSize(ticketCacheSize)
						.softValues()
						.expireAfterAccess(ticketCacheExpiration, TimeUnit.MINUTES)
						.build()
		);
		GuavaCache projects = new GuavaCache(JIRA_PROJECT_CACHE,
				CacheBuilder.newBuilder()
						.maximumSize(projectCacheSize)
						.softValues()
						.expireAfterAccess(projectCacheExpiration, TimeUnit.DAYS)
						.build()
		);
		GuavaCache users = new GuavaCache(
				USERS_CACHE,
				CacheBuilder.newBuilder().maximumSize(userCacheSize).expireAfterWrite(userCacheExpiration, TimeUnit.MINUTES).build()
		);
		GuavaCache projectInfo = new GuavaCache(PROJECT_INFO_CACHE,
				CacheBuilder.newBuilder()
						.maximumSize(projectCacheSize)
						.softValues()
						.expireAfterWrite(projectInfoCacheExpiration, TimeUnit.MINUTES)
						.build()
		);
		//		GuavaCache assignedUsers = new GuavaCache(ASSIGNED_USERS_CACHE, CacheBuilder.newBuilder().maximumSize(userCacheSize).weakKeys()
		//				.expireAfterWrite(userCacheExpiration, TimeUnit.MINUTES).build());

		//@formatter:off
		cacheManager.setCaches(ImmutableList.<GuavaCache> builder()
				.add(tickets)
				.add(projects)
				.add(users)
				.add(projectInfo)
//				.add(assignedUsers)
				.build());
		//@formatter:on
		return cacheManager;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
		Properties p = new Properties();
		p.setProperty("quartz.org.quartz.jobStore.class", "com.epam.ta.TestConfig$NOPJobStore");
		scheduler.setQuartzProperties(p);
		return scheduler;
	}

	public static class NOPJobStore implements JobStore {

		@Override
		public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {

		}

		@Override
		public void schedulerStarted() throws SchedulerException {

		}

		@Override
		public void schedulerPaused() {

		}

		@Override
		public void schedulerResumed() {

		}

		@Override
		public void shutdown() {

		}

		@Override
		public boolean supportsPersistence() {
			return false;
		}

		@Override
		public long getEstimatedTimeToReleaseAndAcquireTrigger() {
			return 0;
		}

		@Override
		public boolean isClustered() {
			return false;
		}

		@Override
		public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger)
				throws ObjectAlreadyExistsException, JobPersistenceException {

		}

		@Override
		public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {

		}

		@Override
		public void storeJobsAndTriggers(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace)
				throws ObjectAlreadyExistsException, JobPersistenceException {

		}

		@Override
		public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
			return false;
		}

		@Override
		public boolean removeJobs(List<JobKey> jobKeys) throws JobPersistenceException {
			return false;
		}

		@Override
		public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
			return null;
		}

		@Override
		public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting)
				throws ObjectAlreadyExistsException, JobPersistenceException {

		}

		@Override
		public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
			return false;
		}

		@Override
		public boolean removeTriggers(List<TriggerKey> triggerKeys) throws JobPersistenceException {
			return false;
		}

		@Override
		public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
			return false;
		}

		@Override
		public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
			return null;
		}

		@Override
		public boolean checkExists(JobKey jobKey) throws JobPersistenceException {
			return false;
		}

		@Override
		public boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException {
			return false;
		}

		@Override
		public void clearAllSchedulingData() throws JobPersistenceException {

		}

		@Override
		public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers)
				throws ObjectAlreadyExistsException, JobPersistenceException {

		}

		@Override
		public boolean removeCalendar(String calName) throws JobPersistenceException {
			return false;
		}

		@Override
		public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
			return null;
		}

		@Override
		public int getNumberOfJobs() throws JobPersistenceException {
			return 0;
		}

		@Override
		public int getNumberOfTriggers() throws JobPersistenceException {
			return 0;
		}

		@Override
		public int getNumberOfCalendars() throws JobPersistenceException {
			return 0;
		}

		@Override
		public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
			return null;
		}

		@Override
		public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
			return null;
		}

		@Override
		public List<String> getJobGroupNames() throws JobPersistenceException {
			return null;
		}

		@Override
		public List<String> getTriggerGroupNames() throws JobPersistenceException {
			return null;
		}

		@Override
		public List<String> getCalendarNames() throws JobPersistenceException {
			return null;
		}

		@Override
		public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
			return null;
		}

		@Override
		public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
			return null;
		}

		@Override
		public void resetTriggerFromErrorState(TriggerKey triggerKey) throws JobPersistenceException {

		}

		@Override
		public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {

		}

		@Override
		public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
			return null;
		}

		@Override
		public void pauseJob(JobKey jobKey) throws JobPersistenceException {

		}

		@Override
		public Collection<String> pauseJobs(GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
			return null;
		}

		@Override
		public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {

		}

		@Override
		public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
			return null;
		}

		@Override
		public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
			return null;
		}

		@Override
		public void resumeJob(JobKey jobKey) throws JobPersistenceException {

		}

		@Override
		public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
			return null;
		}

		@Override
		public void pauseAll() throws JobPersistenceException {

		}

		@Override
		public void resumeAll() throws JobPersistenceException {

		}

		@Override
		public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
			return null;
		}

		@Override
		public void releaseAcquiredTrigger(OperableTrigger trigger) {

		}

		@Override
		public List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggers) throws JobPersistenceException {
			return null;
		}

		@Override
		public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail,
				Trigger.CompletedExecutionInstruction triggerInstCode) {

		}

		@Override
		public void setInstanceId(String schedInstId) {

		}

		@Override
		public void setInstanceName(String schedName) {

		}

		@Override
		public void setThreadPoolSize(int poolSize) {

		}

		@Override
		public long getAcquireRetryDelay(int failureCount) {
			return 0;
		}
	}

}
