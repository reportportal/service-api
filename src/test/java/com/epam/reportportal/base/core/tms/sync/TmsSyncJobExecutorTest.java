package com.epam.reportportal.base.core.tms.sync;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@ExtendWith(MockitoExtension.class)
class TmsSyncJobExecutorTest {

  @Mock
  private TmsSyncJobService tmsSyncJobService;

  @Mock
  private JobExecutionContext jobExecutionContext;

  @InjectMocks
  private TmsSyncJobExecutor executor;

  @Test
  void shouldExecuteSyncSuccessfullyWhenJobIdIsNumeric() throws Exception {
    JobDataMap dataMap = new JobDataMap();
    dataMap.put(TmsSyncJobExecutor.JOB_ID_PARAM, 42L);
    when(jobExecutionContext.getMergedJobDataMap()).thenReturn(dataMap);

    assertDoesNotThrow(() -> executor.execute(jobExecutionContext));

    verify(tmsSyncJobService).executeSync(42L);
  }

  @Test
  void shouldExecuteSyncSuccessfullyWhenJobIdIsStringNumber() throws Exception {
    JobDataMap dataMap = new JobDataMap();
    dataMap.put(TmsSyncJobExecutor.JOB_ID_PARAM, "42");
    when(jobExecutionContext.getMergedJobDataMap()).thenReturn(dataMap);

    assertDoesNotThrow(() -> executor.execute(jobExecutionContext));

    verify(tmsSyncJobService).executeSync(42L);
  }

  @Test
  void shouldNotExecuteWhenJobIdIsMissing() throws Exception {
    JobDataMap dataMap = new JobDataMap();
    when(jobExecutionContext.getMergedJobDataMap()).thenReturn(dataMap);

    assertDoesNotThrow(() -> executor.execute(jobExecutionContext));

    verify(tmsSyncJobService, never()).executeSync(any());
  }

  @Test
  void shouldThrowJobExecutionExceptionWhenServiceFails() {
    JobDataMap dataMap = new JobDataMap();
    dataMap.put(TmsSyncJobExecutor.JOB_ID_PARAM, 42L);
    when(jobExecutionContext.getMergedJobDataMap()).thenReturn(dataMap);

    doThrow(new RuntimeException("Sync error")).when(tmsSyncJobService).executeSync(42L);

    assertThrows(JobExecutionException.class, () -> executor.execute(jobExecutionContext));
  }
}
