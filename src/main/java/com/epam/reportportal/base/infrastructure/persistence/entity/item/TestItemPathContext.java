package com.epam.reportportal.base.infrastructure.persistence.entity.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TestItemPathContext {

  private Long itemId;
  private Long launchId;
  private String path;

}
