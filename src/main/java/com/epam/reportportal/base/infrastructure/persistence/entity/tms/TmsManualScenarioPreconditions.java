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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "tms_manual_scenario_preconditions", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TmsManualScenarioPreconditions implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manual_scenario_id", nullable = false, unique = true)
  private TmsManualScenario manualScenario;

  @Column(name = "value")
  private String value;

  @ManyToMany
  @JoinTable(
      name = "tms_manual_scenario_preconditions_attachment",
      joinColumns = @JoinColumn(name = "preconditions_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  @ToString.Exclude
  @Fetch(FetchMode.SUBSELECT)
  private Set<TmsAttachment> attachments;
}
