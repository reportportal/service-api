package com.epam.reportportal.base.infrastructure.persistence.entity.organization;

/**
 * Constants holder for organization statistics field names used in JSON/persistence mappings. Defines keys for
 * {@code USERS_QUANTITY}, {@code LAUNCHES_QUANTITY}, {@code PROJECTS_QUANTITY}, and {@code LAST_RUN}.
 */
public final class OrganizationStatisticsFields {

  private OrganizationStatisticsFields() {
  }

  public static final String USERS_QUANTITY = "usersQuantity";
  public static final String LAUNCHES_QUANTITY = "launchesQuantity";
  public static final String PROJECTS_QUANTITY = "projectsQuantity";
  public static final String LAST_RUN = "lastRun";

}
