/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.actuate.info.Info;

@ExtendWith(MockitoExtension.class)
class JobSchedulerInfoContributorTest {

  @Mock
  private Scheduler scheduler;

  @InjectMocks
  private JobSchedulerInfoContributor contributor;

  @Test
  @SuppressWarnings("unchecked")
  void contributeShouldSkipTriggersWithoutNextFireTime() throws Exception {
    // given
    JobKey recurringJob = new JobKey("recurring", "group");
    JobKey runningOneShotJob = new JobKey("tmsSyncJob_1", "tmsSync");
    Trigger recurring = trigger("recurringTrigger", new Date(System.currentTimeMillis() + 60_000));
    Trigger oneShot = trigger("tmsSyncJob_1", null);

    when(scheduler.getTriggerGroupNames()).thenReturn(List.of("group", "tmsSync"));
    when(scheduler.getJobKeys(any(GroupMatcher.class)))
        .thenReturn(Set.of(recurringJob))
        .thenReturn(Set.of(runningOneShotJob));
    doReturnTriggers(recurringJob, recurring);
    doReturnTriggers(runningOneShotJob, oneShot);

    // when
    Info.Builder builder = new Info.Builder();
    assertDoesNotThrow(() -> contributor.contribute(builder));

    // then
    Map<String, Map<String, Long>> jobs =
        (Map<String, Map<String, Long>>) builder.build().get("jobs");
    assertEquals(Set.of("recurringTrigger"), jobs.keySet());
    assertTrue(jobs.get("recurringTrigger").get("triggersIn") > 0);
  }

  private void doReturnTriggers(JobKey jobKey, Trigger trigger) throws Exception {
    org.mockito.Mockito.doReturn(List.of(trigger)).when(scheduler).getTriggersOfJob(jobKey);
  }

  private Trigger trigger(String name, Date nextFireTime) {
    Trigger trigger = mock(Trigger.class);
    lenient().when(trigger.getKey()).thenReturn(new TriggerKey(name, "group"));
    when(trigger.getNextFireTime()).thenReturn(nextFireTime);
    return trigger;
  }
}
