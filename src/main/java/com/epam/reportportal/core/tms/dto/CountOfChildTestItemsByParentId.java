package com.epam.reportportal.core.tms.dto;

import lombok.Getter;

@Getter
public class CountOfChildTestItemsByParentId {

  private final Long parentId;
  private final Long count;

  public CountOfChildTestItemsByParentId(Long parentId, Long count) {
    this.parentId = parentId;
    this.count = count;
  }

}
