package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TmsEnvironmentDatasetId implements Serializable {

  @Column(name = "environment_id")
  private Long environmentId;

  @Column(name = "dataset_id")
  private Long datasetId;
}
