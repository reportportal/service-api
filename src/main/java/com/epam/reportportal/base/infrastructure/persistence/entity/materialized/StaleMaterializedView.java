package com.epam.reportportal.base.infrastructure.persistence.entity.materialized;

import java.time.Instant;

/**
 * Bookkeeping row: a materialized view that needs to be refreshed or is dirty.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class StaleMaterializedView {

  private Long id;
  private String name;
  private Instant creationDate;

  public StaleMaterializedView() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Instant getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Instant creationDate) {
    this.creationDate = creationDate;
  }
}
