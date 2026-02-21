package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "tms_attachment", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TmsAttachment implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "file_type")
  private String fileType;

  @Column(name = "file_size")
  private long fileSize;

  @Column(name = "path_to_file")
  private String pathToFile;
  
  @Column(name = "thumbnail_path")
  private String thumbnailPath;

  @Column(name = "expires_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant expiresAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant createdAt;

  @ManyToMany(mappedBy = "attachments")
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsTextManualScenario> textManualScenarios;

  @ManyToMany(mappedBy = "attachments")
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsStep> steps;

  @ManyToMany(mappedBy = "attachments")
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsManualScenarioPreconditions> manualScenarioPreconditions;

  @ManyToMany(mappedBy = "attachments")
  @Fetch(FetchMode.SUBSELECT)
  @ToString.Exclude
  private Set<TmsTestCaseExecutionComment> executionComments;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "environment_id")
  private TmsEnvironment environment;

}