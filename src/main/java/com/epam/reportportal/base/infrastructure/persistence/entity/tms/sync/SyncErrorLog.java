package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSONB-backed collection of synchronization errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncErrorLog implements Serializable {

  @Builder.Default
  private List<SyncError> errors = new ArrayList<>();
}
