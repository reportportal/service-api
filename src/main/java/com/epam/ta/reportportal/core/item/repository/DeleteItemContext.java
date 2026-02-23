package com.epam.ta.reportportal.core.item.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeleteItemContext {

  private Long itemId;
  private Long launchId;
  private String path;

}
