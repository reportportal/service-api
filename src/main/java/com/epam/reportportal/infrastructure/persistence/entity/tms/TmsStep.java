package com.epam.reportportal.infrastructure.persistence.entity.tms;

import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tms_step", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsStep implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "instructions")
  private String instructions;

  @Column(name = "expected_result")
  private String expectedResult;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "steps_manual_scenario_id")
  private TmsStepsManualScenario stepsManualScenario;

  @ManyToMany
  @JoinTable(
      name = "tms_step_attachment",
      joinColumns = @JoinColumn(name = "step_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  @ToString.Exclude
  private Set<TmsAttachment> attachments;

  @ManyToMany
  @JoinTable(
      name = "tms_step_execution",
      joinColumns = @JoinColumn(name = "tms_step_id"),
      inverseJoinColumns = @JoinColumn(name = "test_item_id"))
  @ToString.Exclude
  private Set<TestItem> testItems;
}
