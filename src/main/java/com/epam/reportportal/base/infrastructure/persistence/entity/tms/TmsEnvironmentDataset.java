package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsDatasetType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "dataset_type", nullable = false)
  private TmsDatasetType datasetType;
}
