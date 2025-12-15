package com.epam.reportportal.infrastructure.persistence.entity.tms;

import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing test case execution in a launch. Combines execution data and launch
 * association.
 */
@Entity
@Table(name = "tms_test_case_execution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsTestCaseExecution implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_item_id", unique = true)
  private TestItem testItem;

  @Column(name = "test_case_id")
  private Long testCaseId;

//  @Column(name = "launch_id")
//  private Long launchId;

  @Column(name = "test_case_version_id")
  private Long testCaseVersionId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "test_case_snapshot", nullable = false, columnDefinition = "jsonb")
  private String testCaseSnapshot;
}
