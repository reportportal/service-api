package com.epam.reportportal.base.infrastructure.persistence.entity.item;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NestedItemAttachment {

  private Long itemId;
  private String name;
  private String path;
  private String fileId;
  private String fileName;
  private String contentType;

}
