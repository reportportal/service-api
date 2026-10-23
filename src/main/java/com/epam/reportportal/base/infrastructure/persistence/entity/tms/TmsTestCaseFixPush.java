package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One "Push to agent" of one test case, i.e. one fix round of that case.
 * <p>
 * Example: TC103 came out of iteration #4. The editor comments it and pushes: round 1 starts as {@code PENDING}
 * with {@code previousIterationId} = the id of iteration #4. The agent reports iteration #5 as a re-run of #4,
 * sends the fixed version of TC103 and the round ends {@code FIXED}. Rounds are numbered per test case; a case
 * has at most one {@code PENDING} round.
 */
@Entity
@Table(name = "tms_test_case_fix_push", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseFixPush implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "test_case_id", nullable = false)
  private Long testCaseId;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "round_number", nullable = false)
  private Integer roundNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TmsFixPushStatus status;

  /**
   * The iteration that produced the case; the agent reports its fix as a re-run of it.
   */
  @Column(name = "previous_iteration_id", nullable = false)
  private Long previousIterationId;

  /**
   * The iteration the agent reported for this round, once it has.
   */
  @Column(name = "result_iteration_id")
  private Long resultIterationId;

  @Column(name = "requested_by", nullable = false)
  private Long requestedBy;

  @Column(name = "requested_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant requestedAt;

  @Column(name = "finished_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant finishedAt;

  @Column(name = "workflow_url", columnDefinition = "TEXT")
  private String workflowUrl;

  @Column(name = "failure_reason", columnDefinition = "TEXT")
  private String failureReason;
}
