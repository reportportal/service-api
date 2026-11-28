package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * TMS list settings of one user in one project (items per page, sort mode, tree view mode, ...),
 * stored as a flat JSON object. The keys are owned by the UI.
 */
@Entity
@Table(name = "tms_user_settings", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsUserSettings {

  @EmbeddedId
  private TmsUserSettingsId id;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "settings", nullable = false)
  private Map<String, Object> settings;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
