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
import jakarta.persistence.OneToOne;
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
@Table(name = "tms_test_case", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCase implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "priority")
  private String priority;

  @Column(name = "search_vector", insertable = false, updatable = false)
  private String searchVector; //immutable, because trigger updates this field

  @Column(name = "external_id")
  private String externalId;

  @OneToMany(mappedBy = "testCase", fetch = FetchType.LAZY)
  @ToString.Exclude
  private Set<TmsTestCaseAttribute> tags;

  @OneToMany(mappedBy = "testCase", fetch =  FetchType.LAZY)
  @ToString.Exclude
  private Set<TmsTestCaseVersion> versions;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_folder_id", nullable = false)
  private TmsTestFolder testFolder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dataset_id")
  private TmsDataset dataset;

  @ManyToMany
  @JoinTable(
      name = "tms_test_case_test_item",
      joinColumns = @JoinColumn(name = "test_case_id"),
      inverseJoinColumns = @JoinColumn(name = "test_item_id"))
  @ToString.Exclude
  private Set<TestItem> testItems;
}
