package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsLifecycleActorType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Who made a Ready test case Ready.
 * <p>
 * Example by a person: {@code {"type": "USER", "user": {"id": 7, "login": "user1", "fullName": "Ann Smith"},
 * "at": "2026-11-21T10:00:00Z"}}. By Auto-Ready: {@code {"type": "AUTO_READY", "at": "..."}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsMadeReadyByRS {

  /**
   * {@code USER} or {@code AUTO_READY}.
   */
  private TmsLifecycleActorType type;

  /**
   * The person, for {@code type = USER}; absent when the user no longer exists.
   */
  private TmsUserRS user;

  private Instant at;
}
