package com.epam.ta.reportportal.core.tms.db.entity;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.item.TestItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tms_test_folder", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class TmsTestFolder implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, precision = 64)
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @OneToMany(mappedBy = "testFolder")
  private List<TmsTestCase> testCases;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private TmsTestFolder parentTestFolder;

  @OneToMany(mappedBy = "parentTestFolder")
  private List<TmsTestFolder> subFolders;

  @ManyToMany(mappedBy = "testFolders")
  @ToString.Exclude
  private Set<TmsTestPlan> testPlans;

  @ManyToMany
  @JoinTable(
      name = "tms_test_folder_test_item",
      joinColumns = @JoinColumn(name = "test_folder_id"),
      inverseJoinColumns = @JoinColumn(name = "test_item_id"))
  @ToString.Exclude
  private Set<TestItem> testItems;
}
