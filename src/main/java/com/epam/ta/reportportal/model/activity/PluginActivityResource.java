package com.epam.ta.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginActivityResource {
  @JsonProperty(value = "id", required = true)
  private Long id;
  @JsonProperty(value = "name")
  private String name;

  @JsonProperty(value = "enabled")
  private boolean enabled;

  @JsonProperty(value = "version")
  private String version;

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

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "PluginActivityResource{" + "id=" + id + ", name='" + name + '\'' + ", enabled="
        + enabled + ", version='" + version + '\'' + '}';
  }
}
