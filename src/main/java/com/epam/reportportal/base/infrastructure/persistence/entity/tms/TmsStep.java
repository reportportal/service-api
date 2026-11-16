package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

  @Column(name = "instructions", columnDefinition = "TEXT")
  private String instructions;

  @Column(name = "expected_result", columnDefinition = "TEXT")
  private String expectedResult;

  @Column(name = "number", nullable = false)
  private Integer number = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "steps_manual_scenario_id")
  private TmsStepsManualScenario stepsManualScenario;

  @ManyToMany
  @JoinTable(
      name = "tms_step_attachment",
      joinColumns = @JoinColumn(name = "step_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  @ToString.Exclude
  @Fetch(FetchMode.SUBSELECT)
  private Set<TmsAttachment> attachments;
}
