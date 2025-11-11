package com.epam.reportportal.infrastructure.persistence.entity.onboarding;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author Antonov Maksim
 */
public class Onboarding implements Serializable {

  private Long id;
  private String page;
  private String data;
  private Instant availableFrom;
  private Instant availableTo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Instant getAvailableFrom() {
    return availableFrom;
  }

  public void setAvailableFrom(Instant availableFrom) {
    this.availableFrom = availableFrom;
  }

  public Instant getAvailableTo() {
    return availableTo;
  }

  public void setAvailableTo(Instant availableTo) {
    this.availableTo = availableTo;
  }
}
