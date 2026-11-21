
package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "tms_manual_scenario_requirement", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class TmsManualScenarioRequirement implements Persistable<String> {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "value")
  private String value;

  @Column(name = "number", nullable = false)
  private Integer number = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manual_scenario_id", nullable = false)
  private TmsManualScenario manualScenario;

  @Getter(AccessLevel.NONE)
  @Builder.Default
  @Transient
  private boolean isNew = true;

  /*
   * The id is a client-generated String (not DB-generated), so the default Spring Data
   * "id == null means new" check always sees a non-null id and routes save()/saveAll() through
   * merge() (which issues a SELECT before every INSERT). Persistable restores the fast,
   * SELECT-free persist() path for entities built in memory, while @PostLoad keeps correct
   * merge/update semantics for instances actually fetched from the database.
   */
  @Override
  public boolean isNew() {
    return isNew;
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    isNew = false;
  }
}
