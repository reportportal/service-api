package com.epam.reportportal.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing a comment for test case execution.
 */
@Entity
@Table(name = "tms_test_case_execution_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class TmsTestCaseExecutionComment implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "execution_id", nullable = false, unique = true)
  @ToString.Exclude
  private TmsTestCaseExecution execution;

  @Column(name = "comment", columnDefinition = "text")
  private String comment;

  @Column(name = "bts_ticket_id")
  private Long btsTicketId;

  @ManyToMany
  @JoinTable(
      name = "tms_test_case_execution_comment_attachment",
      joinColumns = @JoinColumn(name = "execution_comment_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  @ToString.Exclude
  private Set<TmsAttachment> attachments;
}
