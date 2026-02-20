package com.epam.ta.reportportal.core.item.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PreviousTryProjection {

  private Long itemId;
  private Long launchId;
  private String path;

}
