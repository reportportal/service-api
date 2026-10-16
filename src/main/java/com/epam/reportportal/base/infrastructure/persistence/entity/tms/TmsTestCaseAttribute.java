package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "tms_test_case_attribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseAttribute implements Serializable, Persistable<TmsTestCaseAttributeId> {

  @EmbeddedId
  private TmsTestCaseAttributeId id;

  @ManyToOne
  @MapsId(value = "testCaseId")
  @JoinColumn(name = "test_case_id")
  private TmsTestCase testCase;

  @ManyToOne
  @MapsId(value = "attributeId")
  @JoinColumn(name = "attribute_id")
  private TmsAttribute attribute;

  @Getter(AccessLevel.NONE)
  @Transient
  private boolean isNew = true;

  /*
   * The id is assigned by application code before the entity is ever persisted, so the default
   * Spring Data "id == null means new" check always sees a non-null id and routes save()/saveAll()
   * through merge() (which issues a SELECT before every INSERT). Persistable restores the fast,
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

  //TODO: override equals and hashCode methods
}
