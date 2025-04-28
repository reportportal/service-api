/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.plugin;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class represents information about a plugin, including its ID, version, file ID, file name,
 * enabled status, and additional details.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PluginDetails implements Serializable {

  private String id;
  private String name;
  private String version;
  private String license;
  private String description;
  private String documentation;
  private String requires;
  private final Map<String, Object> developer = new HashMap<>();
  private final Map<String, Object> metadata = new HashMap<>();
  private final Map<String, Object> properties = new HashMap<>();
  private final Map<String, Object> binaryData = new HashMap<>();

  /**
   * Sets the developer information for the plugin.
   *
   * @param name the name of the developer
   * @param url  the Website URL of the developer
   */
  public void setDeveloper(String name, String url) {
    if (name != null && !name.isEmpty()) {
      this.developer.put("name", name);
    }
    if (url != null && !url.isEmpty()) {
      this.developer.put("website", url);
    }
  }

  /**
   * Sets the metadata for the plugin.
   *
   * @param metadata a {@link Map} of metadata key-value pairs
   */
  public void setMetadata(Map<String, Object> metadata) {
    if (!metadata.isEmpty()) {
      this.metadata.putAll(metadata);
    }
  }

  /**
   * Sets the metadata for the plugin.
   *
   * @param key   the metadata key
   * @param value the metadata value
   */
  public void setMetadata(String key, Object value) {
    if (key != null && !key.isEmpty()) {
      this.metadata.put(key, value);
    }
  }

  /**
   * Sets the properties for the plugin.
   *
   * @param properties a {@link Map} of property key-value pairs
   */
  public void setProperties(Map<String, Object> properties) {
    if (!properties.isEmpty()) {
      this.properties.putAll(properties);
    }
  }

  /**
   * Sets the properties for the plugin.
   *
   * @param key   the property key
   * @param value the property value
   */
  public void setProperties(String key, Object value) {
    if (key != null && !key.isEmpty()) {
      this.properties.put(key, value);
    }
  }

  /**
   * Converts the {@link PluginDetails} to a map representation.
   *
   * @return a {@link Map} of {@link String} keys and {@link Object} values representing
   *     the {@link PluginDetails}
   *
   */
  public Map<String, Object> toMap() {
    Map<String, Object> details = new HashMap<>();

    for (Field field : this.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object value = field.get(this);
        if (value != null) {
          details.put(field.getName(), value);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Failed to access field: " + field.getName(), e);
      }
    }

    return details;
  }

  /**
   * Builder class for creating instances of {@link PluginDetails}.
   */
  public static class Builder {

    private final PluginDetails details = new PluginDetails();

    /**
     * Sets the ID of the plugin.
     *
     * @param id the ID of the plugin
     * @return the builder instance
     */
    public Builder id(String id) {
      details.setId(id);
      return this;
    }

    /**
     * Sets the name of the plugin.
     *
     * @param name the name of the plugin
     * @return the builder instance
     */
    public Builder name(String name) {
      details.setName(name);
      return this;
    }

    /**
     * Sets the version of the plugin.
     *
     * @param version the version of the plugin
     * @return the builder instance
     */
    public Builder version(String version) {
      details.setVersion(version);
      return this;
    }

    /**
     * Sets the license of the plugin.
     *
     * @param license the license of the plugin
     * @return the builder instance
     */
    public Builder license(String license) {
      details.setLicense(license);
      return this;
    }

    /**
     * Sets the description of the plugin.
     *
     * @param description the description of the plugin
     * @return the builder instance
     */
    public Builder description(String description) {
      details.setDescription(description);
      return this;
    }

    /**
     * Sets the documentation of the plugin.
     *
     * @param documentation the documentation of the plugin
     * @return the builder instance
     */
    public Builder documentation(String documentation) {
      details.setDocumentation(documentation);
      return this;
    }

    /**
     * Sets the required plugins for the plugin.
     *
     * @param requires the required plugins
     * @return the builder instance
     */
    public Builder requires(String requires) {
      details.setRequires(requires);
      return this;
    }

    /**
     * Sets the developer information for the plugin.
     *
     * @param name the name of the developer
     * @param url  the Website URL of the developer
     * @return the builder instance
     */
    public Builder developer(String name, String url) {
      details.setDeveloper(name, url);
      return this;
    }

    /**
     * Sets the developer information for the plugin.
     *
     * @param name the name of the developer
     * @return the builder instance
     */
    public Builder developer(String name) {
      details.setDeveloper(name, null);
      return this;
    }

    /**
     * Sets the metadata for the plugin.
     *
     * @param metadata a {@link Map} of metadata key-value pairs
     * @return the builder instance
     */
    public Builder metadata(Map<String, Object> metadata) {
      details.setMetadata(metadata);
      return this;
    }

    /**
     * Sets the metadata for the plugin.
     *
     * @param key   the metadata key
     * @param value the metadata value
     * @return the builder instance
     */
    public Builder metadata(String key, Object value) {
      details.setMetadata(key, value);
      return this;
    }

    /**
     * Sets the properties for the plugin.
     *
     * @param properties a {@link Map} of property key-value pairs
     * @return the builder instance
     */
    public Builder properties(Map<String, Object> properties) {
      details.setProperties(properties);
      return this;
    }

    /**
     * Sets the properties for the plugin.
     *
     * @param key   the property key
     * @param value the property value
     * @return the builder instance
     */
    public Builder properties(String key, Object value) {
      details.setProperties(key, value);
      return this;
    }

    /**
     * Sets the binary data for the plugin.
     *
     * @param binaryData a {@link Map} of binary data key-value pairs
     * @return the builder instance
     */
    public Builder binaryData(Map<String, Object> binaryData) {
      details.setProperties(binaryData);
      return this;
    }

    /**
     * Builds and returns the {@link PluginDetails} instance.
     *
     * @return the built {@link PluginDetails} instance
     */
    public PluginDetails build() {
      return details;
    }

  }

  /**
   * Creates a new {@link Builder} instance for constructing {@link PluginDetails}.
   *
   * @return a new {@link Builder} instance
   */
  public static Builder builder() {
    return new Builder();
  }
}
