package com.epam.reportportal.base.infrastructure.persistence.entity.organization;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationProfile {

  private Long id;
  private Instant createdAt;
  private Instant updatedAt;
  private String name;
  private String slug;
  private String externalId;
  private String type;
  private Long ownerId;
  private Integer launchesQuantity;
  private Instant lastRun;
  private Integer projectsQuantity;
  private Integer usersQuantity;

}
