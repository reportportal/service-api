package com.epam.reportportal.base.core.tms.sync.dto;

import com.epam.reportportal.base.core.tms.enums.TmsSyncDirection;
import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.enums.TmsSyncStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncCounters;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncErrorLog;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncScopeConfig;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsSyncJobRS {
    private Long id;
    private Long projectId;
    private Long integrationId;
    private TmsSyncProvider provider;
    private TmsSyncDirection direction;
    private TmsSyncStatus status;
    private SyncScopeConfig scopeConfig;
    private SyncCounters counters;
    private SyncErrorLog errorLog;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
}