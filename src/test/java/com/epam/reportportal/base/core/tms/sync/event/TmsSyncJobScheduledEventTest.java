package com.epam.reportportal.base.core.tms.sync.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TmsSyncJobScheduledEventTest {

  @Test
  void shouldStoreAndReturnJobId() {
    Object source = new Object();
    Long jobId = 100L;

    TmsSyncJobScheduledEvent event = new TmsSyncJobScheduledEvent(source, jobId);

    assertEquals(jobId, event.getJobId());
    assertEquals(source, event.getSource());
  }
}

