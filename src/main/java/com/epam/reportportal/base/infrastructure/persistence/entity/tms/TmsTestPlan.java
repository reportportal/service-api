package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "tms_test_plan", schema = "public")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant updatedAt;

  @Column(name = "search_vector", insertable = false, updatable = false)
  private String searchVector; //immutable, because trigger updates this field

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne
  @JoinColumn(name = "environment_id", nullable = false)
  private TmsEnvironment environment;

  @ManyToOne
  @JoinColumn(name = "product_version_id", nullable = false)
  private TmsProductVersion productVersion;

  @OneToMany
  @JoinColumn(name = "test_plan_id", referencedColumnName = "id", insertable = false, updatable = false)
  @ToString.Exclude
  private Set<Launch> launches;

  @OneToMany(mappedBy = "testPlan")
  @ToString.Exclude
  private Set<TmsTestPlanAttribute> attributes;

  @OneToMany(mappedBy = "testPlan")
  @ToString.Exclude
  private Set<TmsMilestone> milestones;

  @ManyToMany
  @JoinTable(
      name = "tms_test_plan_test_case",
      joinColumns = @JoinColumn(name = "test_plan_id"),
      inverseJoinColumns = @JoinColumn(name = "test_case_id"))
  @ToString.Exclude
  private Set<TmsTestCase> testCases;
}
