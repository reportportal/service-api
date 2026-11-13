package com.epam.reportportal.base.core.tms.sync;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.sync.event.TmsSyncJobScheduledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class TmsSyncJobSchedulerTest {

  @Mock
  private Scheduler scheduler;

  @Test
  @SuppressWarnings("unchecked")
  void shouldScheduleJobSuccessfully() throws Exception {
    ObjectProvider<Scheduler> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(scheduler);

    TmsSyncJobScheduler jobScheduler = new TmsSyncJobScheduler(provider);
    TmsSyncJobScheduledEvent event = new TmsSyncJobScheduledEvent(this, 100L);

    assertDoesNotThrow(() -> jobScheduler.onTmsSyncJobScheduled(event));

    ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
    ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

    verify(scheduler).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());

    JobDetail jobDetail = jobDetailCaptor.getValue();
    assertEquals("tmsSyncJob_100", jobDetail.getKey().getName());
    assertEquals("tmsSync", jobDetail.getKey().getGroup());
    assertEquals(100L, jobDetail.getJobDataMap().get(TmsSyncJobExecutor.JOB_ID_PARAM));

    Trigger trigger = triggerCaptor.getValue();
    assertEquals("tmsSyncTrigger_100", trigger.getKey().getName());
    assertEquals("tmsSync", trigger.getKey().getGroup());
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldThrowExceptionWhenSchedulerIsNull() {
    ObjectProvider<Scheduler> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(null);

    TmsSyncJobScheduler jobScheduler = new TmsSyncJobScheduler(provider);
    TmsSyncJobScheduledEvent event = new TmsSyncJobScheduledEvent(this, 100L);

    assertDoesNotThrow(() -> jobScheduler.onTmsSyncJobScheduled(event));
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldThrowExceptionWhenSchedulerFails() throws Exception {
    ObjectProvider<Scheduler> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(scheduler);

    doThrow(new SchedulerException("Scheduler error"))
        .when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

    TmsSyncJobScheduler jobScheduler = new TmsSyncJobScheduler(provider);
    TmsSyncJobScheduledEvent event = new TmsSyncJobScheduledEvent(this, 100L);

    assertDoesNotThrow(() -> jobScheduler.onTmsSyncJobScheduled(event));

    verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }
}