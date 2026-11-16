package com.epam.reportportal.base.core.tms.sync.event;

import com.epam.reportportal.base.core.tms.sync.TmsSyncJobScheduler;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a TMS sync job is scheduled.
 * This event is handled by {@link TmsSyncJobScheduler} after the transaction commits.
 */
@Getter
public class TmsSyncJobScheduledEvent extends ApplicationEvent {
  private final Long jobId;

  public TmsSyncJobScheduledEvent(Object source, Long jobId) {
    super(source);
    this.jobId = jobId;
  }

}

