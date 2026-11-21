package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Synchronization job scope configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncScopeConfig implements Serializable {

  private String remoteFolderId;
  private Long localFolderId;
}
