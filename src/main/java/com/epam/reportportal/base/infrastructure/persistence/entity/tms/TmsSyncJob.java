package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.core.tms.enums.TmsSyncDirection;
import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.enums.TmsSyncStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncCounters;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncErrorLog;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncScopeConfig;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Persists the state and configuration of a TMS synchronization operation.
 */
@Entity
@Table(name = "tms_sync_job", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsSyncJob implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "integration_id")
  private Integration integration;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false)
  private TmsSyncProvider provider;

  @Enumerated(EnumType.STRING)
  @Column(name = "direction", nullable = false)
  private TmsSyncDirection direction;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TmsSyncStatus status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "scope_config", columnDefinition = "jsonb")
  private SyncScopeConfig scopeConfig;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "counters", columnDefinition = "jsonb")
  private SyncCounters counters;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "error_log", columnDefinition = "jsonb")
  private SyncErrorLog errorLog;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;
}
