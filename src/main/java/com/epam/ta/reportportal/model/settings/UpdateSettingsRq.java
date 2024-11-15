package com.epam.ta.reportportal.model.settings;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateSettingsRq {

  @NotEmpty
  private String key;
  @NotEmpty
  private String value;

}
