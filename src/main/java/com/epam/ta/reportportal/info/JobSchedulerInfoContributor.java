package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.exception.ReportPortalException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collects info about existing jobs and how much it is left
 * to the next trigger execution
 *
 * @author Pavel Bortnik
 */
@Component
public class JobSchedulerInfoContributor implements InfoContributor {

	@Autowired
	private Scheduler scheduler;

	@Override
	public void contribute(Info.Builder builder) {
		try {
			List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
			List<JobKey> jobKeys = triggerGroupNames.stream().flatMap(name -> {
				try {
					return scheduler.getJobKeys(GroupMatcher.groupEquals(name)).stream();
				} catch (SchedulerException e) {
					throw new ReportPortalException(e.getMessage());
				}
			}).collect(Collectors.toList());
			Map<String, Map<String, Long>> triggersInfo = jobKeys.stream().flatMap(key -> {
				try {
					return scheduler.getTriggersOfJob(key).stream();
				} catch (SchedulerException e) {
					throw new ReportPortalException(e.getMessage());
				}
			}).collect(Collectors.toMap(
					t -> t.getKey().getName(),
					t -> Collections.singletonMap("triggersIn", t.getNextFireTime().getTime() - System.currentTimeMillis())
			));

			builder.withDetail("jobs", triggersInfo);
		} catch (SchedulerException e) {
			throw new ReportPortalException(e.getMessage());
		}
	}
}
