package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key of {@link TmsUserSettings}: one row per user and project.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TmsUserSettingsId implements Serializable {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "project_id")
  private Long projectId;
}
