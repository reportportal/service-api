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
import com.epam.ta.reportportal.util.analyzer.AnalyzerServiceClient;
import com.github.fakemongo.Fongo;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.mongodb.MockMongoClient;
import com.mongodb.WriteConcern;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import java.util.concurrent.TimeUnit;

import static com.epam.ta.reportportal.config.CacheConfiguration.*;
import static org.mockito.Mockito.mock;

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

	@Configuration
	@Profile("unittest")
	static class Config {
		@Bean
		@SuppressWarnings("unchecked")
		public RedisSerializer<Object> defaultRedisSerializer() {
			return mock(RedisSerializer.class);
		}

		@Bean
		@Primary
		public RedisConnectionFactory connectionFactory() {
			RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
			RedisConnection connection = mock(RedisConnection.class);
			Mockito.when(factory.getConnection()).thenReturn(connection);

			return factory;
		}

		@Bean
		public AnalyzerServiceClient analyzerServiceClient()
		{
			return mock(AnalyzerServiceClient.class);
		}
	}

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
		return Mockito.mock(UatClient.class);
	}

	@Bean
	public OAuth2ProtectedResourceDetails oauthResource(){
		return Mockito.mock(OAuth2ProtectedResourceDetails.class);
	}


	@Bean
	public MongoFixtureImporter mongoFixtureImporter() {
		return new MongoFixtureImporter();
	}

	@Bean
	@Primary
	public CacheManager getGlobalCacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();

		GuavaCache tickets = new GuavaCache(EXTERNAL_SYSTEM_TICKET_CACHE, CacheBuilder.newBuilder().maximumSize(ticketCacheSize)
				.softValues().expireAfterAccess(ticketCacheExpiration, TimeUnit.MINUTES).build());
		GuavaCache projects = new GuavaCache(JIRA_PROJECT_CACHE, CacheBuilder.newBuilder().maximumSize(projectCacheSize).softValues()
				.expireAfterAccess(projectCacheExpiration, TimeUnit.DAYS).build());
		GuavaCache users = new GuavaCache(USERS_CACHE,
				CacheBuilder.newBuilder().maximumSize(userCacheSize).expireAfterWrite(userCacheExpiration, TimeUnit.MINUTES).build());
		GuavaCache projectInfo = new GuavaCache(PROJECT_INFO_CACHE, CacheBuilder.newBuilder().maximumSize(projectCacheSize).softValues()
				.expireAfterWrite(projectInfoCacheExpiration, TimeUnit.MINUTES).build());
		GuavaCache assignedUsers = new GuavaCache(ASSIGNED_USERS_CACHE, CacheBuilder.newBuilder().maximumSize(userCacheSize).weakKeys()
				.expireAfterWrite(userCacheExpiration, TimeUnit.MINUTES).build());

		//@formatter:off
		cacheManager.setCaches(ImmutableList.<GuavaCache> builder()
				.add(tickets)
				.add(projects)
				.add(users)
				.add(projectInfo)
				.add(assignedUsers)
				.build());
		//@formatter:on
		return cacheManager;
	}

}