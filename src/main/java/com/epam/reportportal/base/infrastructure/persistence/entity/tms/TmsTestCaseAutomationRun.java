package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * One test case sent to automation in one iteration of the Test automation pipeline.
 * <p>
 * Example: the editor presses Automate on TC42 and TC43 against {@code beta5}: both get a run with the same
 * {@code iterationId} (iteration #8 of "Test automation"), {@code environment = "beta5"} and the default version
 * id the case had at that moment. Whether the case is in progress, automated or failed follows from that
 * iteration; nothing in the run is updated later.
 */
@Entity
@Table(name = "tms_test_case_automation_run", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseAutomationRun implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "test_case_id", nullable = false)
  private Long testCaseId;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "iteration_id", nullable = false)
  private Long iterationId;

  @Column(name = "environment", nullable = false)
  private String environment;

  /**
   * The default version of the case when it was sent; a different default version later means the scenario
   * changed after automation.
   */
  @Column(name = "test_case_version_id")
  private Long testCaseVersionId;

  @Column(name = "requested_by", nullable = false)
  private Long requestedBy;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "workflow_url", columnDefinition = "TEXT")
  private String workflowUrl;
}
