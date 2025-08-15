package com.epam.ta.reportportal.core.tms.db.entity;

import com.epam.ta.reportportal.entity.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tms_environment", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsEnvironment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @OneToMany(mappedBy = "environment")
  @ToString.Exclude
  private Set<TmsAttachment> attachments;

  @OneToMany(mappedBy = "environment")
  @ToString.Exclude
  private Set<TmsTestPlan> testPlans;

  @OneToMany(mappedBy = "environment")
  @ToString.Exclude
  private Set<TmsEnvironmentDataset> environmentDatasets;
}
