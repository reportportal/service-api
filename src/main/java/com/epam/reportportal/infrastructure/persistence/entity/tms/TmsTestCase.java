package com.epam.reportportal.infrastructure.persistence.entity.tms;

import com.epam.reportportal.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "tms_test_case", schema = "public")
@EntityListeners(AuditingEntityListener.class)
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

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "priority")
  private String priority;

  @Column(name = "search_vector", insertable = false, updatable = false)
  private String searchVector; //immutable, because trigger updates this field

  @Column(name = "external_id")
  private String externalId;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant updatedAt;

  @OneToMany(mappedBy = "testCase", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsTestCaseAttribute> attributes;

  @OneToMany(mappedBy = "testCase", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsTestCaseVersion> versions;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_folder_id", nullable = false)
  private TmsTestFolder testFolder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dataset_id")
  private TmsDataset dataset;

  @ManyToMany(mappedBy = "testCases")
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsTestPlan> testPlans;
}
