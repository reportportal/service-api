package com.epam.reportportal.base.infrastructure.persistence.entity.project;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectProfile {

  private Long id;
  private Long organizationId;
  private Instant createdAt;
  private Instant updatedAt;
  private String key;
  private String slug;
  private String name;

  private Integer launchesQuantity;
  private Instant lastRun;
  private Integer usersQuantity;
}
