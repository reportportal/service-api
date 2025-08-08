package com.epam.ta.reportportal.core.tms.db.entity;

import com.epam.ta.reportportal.entity.item.TestItem;
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
import jakarta.persistence.OneToMany;
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

  @Column(name = "instructions")
  private String instructions;

  @Column(name = "expected_result")
  private String expectedResult;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "steps_manual_scenario_id")
  private TmsStepsManualScenario stepsManualScenario;

  @OneToMany(mappedBy = "step")
  @Fetch(FetchMode.SUBSELECT)
  private Set<TmsAttachment> attachments;

  @ManyToMany
  @JoinTable(
      name = "tms_step_test_item",
      joinColumns = @JoinColumn(name = "step_id"),
      inverseJoinColumns = @JoinColumn(name = "test_item_id"))
  @ToString.Exclude
  private Set<TestItem> testItems;
}
