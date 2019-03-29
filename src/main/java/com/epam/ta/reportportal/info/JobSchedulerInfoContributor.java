/*
 * Copyright 2018 EPAM Systems
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
package com.epam.ta.reportportal.info;

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
import org.springframework.context.annotation.Profile;
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
@Profile("jobs")
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
