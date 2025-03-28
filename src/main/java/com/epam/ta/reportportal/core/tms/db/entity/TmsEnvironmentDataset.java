package com.epam.ta.reportportal.core.tms.db.entity;

import com.epam.ta.reportportal.core.tms.db.entity.enums.TmsDatasetType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "tms_environment_dataset", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsEnvironmentDataset {

  @EmbeddedId
  private TmsEnvironmentDatasetId id;

  @ManyToOne
  @MapsId(value = "environmentId")
  @JoinColumn(name = "environment_id")
  private TmsEnvironment environment;

  @ManyToOne
  @MapsId(value = "datasetId")
  @JoinColumn(name = "dataset_id")
  private TmsDataset dataset;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "dataset_type", nullable = false)
  private TmsDatasetType datasetType;
}
