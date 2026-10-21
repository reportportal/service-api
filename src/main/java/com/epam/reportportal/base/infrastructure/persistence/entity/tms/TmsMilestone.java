package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tms_milestone", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsMilestone {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "display_id")
  private String displayId;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(name = "start_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime startDate;

  @Column(name = "end_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime endDate;

  @Column(name = "type", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private TmsMilestoneType type;

  @Column(name = "status", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private TmsMilestoneStatus status;

  @ManyToOne
  @JoinColumn(name = "product_version_id", nullable = false)
  private TmsProductVersion productVersion;

  @OneToMany(mappedBy = "milestone")
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsTestPlan> testPlans;
}
