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
package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.core.configs.Conditions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.apache.commons.collections.MapUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collects info about existing jobs and how much it is left
 * to the next trigger execution
 *
 * @author Pavel Bortnik
 */
@Component
@Conditional(Conditions.NotTestCondition.class)
public class JobSchedulerInfoContributor implements InfoContributor {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerInfoContributor.class);

	@Autowired
	private Scheduler scheduler;

	@Override
	public void contribute(Info.Builder builder) {
		try {
			Map<String, Map<String, Long>> triggersInfo = scheduler.getTriggerGroupNames().stream().flatMap(name -> {
				try {
					return scheduler.getJobKeys(GroupMatcher.groupEquals(name)).stream();
				} catch (SchedulerException e) {
					LOGGER.warn(e.getMessage());
					return Stream.empty();
				}
			}).collect(Collectors.toList()).stream().flatMap(key -> {
				try {
					return scheduler.getTriggersOfJob(key).stream();
				} catch (SchedulerException e) {
					LOGGER.warn(e.getMessage());
					return Stream.empty();
				}
			}).collect(Collectors.toMap(
					t -> t.getKey().getName(),
					t -> Collections.singletonMap("triggersIn", t.getNextFireTime().getTime() - System.currentTimeMillis())
			));
			if (MapUtils.isNotEmpty(triggersInfo)) {
				builder.withDetail("jobs", triggersInfo);
			}
		} catch (SchedulerException e) {
			throw new ReportPortalException(e.getMessage());
		}
	}
}
