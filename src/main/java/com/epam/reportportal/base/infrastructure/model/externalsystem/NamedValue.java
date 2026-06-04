package com.epam.reportportal.base.infrastructure.model.externalsystem;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents value that consists of id for making request and human readable name
 */
@Data
@NoArgsConstructor
public class NamedValue implements Serializable {

  private Long id;
  private String name;
}
