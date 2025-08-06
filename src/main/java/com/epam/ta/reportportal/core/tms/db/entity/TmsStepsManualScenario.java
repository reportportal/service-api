package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "tms_steps_manual_scenario", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsStepsManualScenario {

  @Id
  @Column(name = "manual_scenario_id")
  private Long manualScenarioId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manual_scenario_id")
  @MapsId
  private TmsManualScenario manualScenario;

  @OneToMany(mappedBy = "stepsManualScenario")
  @Fetch(FetchMode.SUBSELECT)
  private Set<TmsStep> steps;
}
