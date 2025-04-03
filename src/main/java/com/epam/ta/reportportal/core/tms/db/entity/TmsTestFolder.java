package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tms_test_folder", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class TmsTestFolder implements Serializable {

  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  @Column(
      name = "id",
      unique = true,
      nullable = false,
      precision = 64
  )
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  //TODO to become foreign key
  @Column(name = "project_id")
  private Long projectId;

  @OneToMany(mappedBy = "testFolder")
  private List<TmsTestCase> testCases;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private TmsTestFolder parent;

  @OneToMany(mappedBy = "parent")
  private List<TmsTestFolder> subTestFolders;

//    @ManyToMany(mappedBy = "testFolders") TODO add
//    private Set<TmsTestPlan> testPlans;

  public TmsTestFolder(final Long id, final Long projectId, final String name,
      final String description) {
    this.id = id;
    this.projectId = projectId;
    this.name = name;
    this.description = description;
  }
}
