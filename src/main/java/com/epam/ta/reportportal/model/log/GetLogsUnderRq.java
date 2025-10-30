package com.epam.ta.reportportal.model.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class GetLogsUnderRq {

  @NotNull
  @JsonProperty(value = "itemIds")
  private List<Long> itemIds;

  @NotNull
  @JsonProperty(value = "logLevel")
  private String logLevel;

  public GetLogsUnderRq() {
  }

  public List<Long> getItemIds() {
    return itemIds;
  }

  public void setItemIds(List<Long> itemIds) {
    this.itemIds = itemIds;
  }

  public String getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }
}
