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

package com.epam.ta.reportportal.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.pf4j.Plugin;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.util.StringUtils;

/**
 * Report Portal plugin descriptor. This class is used to describe the properties of a plugin in
 * Report Portal. It includes information such as the plugin ID, name, description, version,
 * dependencies, and other metadata.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DetailPluginDescriptor implements PluginDescriptor {

  private String pluginId;
  private String pluginName;
  private String pluginDescription;
  private String pluginClass = Plugin.class.getName();
  private String version;
  private String requires = "*";
  private String provider;
  private String license;
  private String documentation;
  private List<PluginDependency> dependencies = new ArrayList<>();
  private Map<String, Object> metadata = new HashMap<>();
  private Map<String, Object> properties = new HashMap<>();
  private Map<String, Object> binaryData = new HashMap<>();

  /**
   * Sets the plugin dependencies.
   *
   * @param dependencies the plugin dependencies as a comma-separated string
   */
  protected void setDependencies(String dependencies) {
    this.dependencies.clear();

    if (dependencies != null) {
      dependencies = dependencies.trim();
      if (!dependencies.isEmpty()) {
        setDependencies(dependencies.split(","));
      }
    }
  }

  /**
   * Sets the plugin dependencies.
   *
   * @param dependencies the plugin dependencies as an array of strings
   */
  protected void setDependencies(String... dependencies) {
    for (String dependency : dependencies) {
      dependency = dependency.trim();
      if (!dependency.isEmpty()) {
        this.dependencies.add(new PluginDependency(dependency));
      }
    }
  }

  /**
   * Builder for {@link DetailPluginDescriptor}. This builder allows you to create an instance of
   * {@link DetailPluginDescriptor} with a fluent API.
   */
  public static class Builder {

    private final DetailPluginDescriptor descriptor = new DetailPluginDescriptor();

    /**
     * Sets the plugin ID.
     *
     * @param id the plugin ID
     * @return the current instance of {@link Builder}
     */
    public Builder pluginId(String id) {
      descriptor.setPluginId(id);
      return this;
    }

    /**
     * Sets the plugin name.
     *
     * @param name the plugin name
     * @return the current instance of {@link Builder}
     */
    public Builder pluginName(String name) {
      descriptor.setPluginName(name);
      return this;
    }

    /**
     * Sets the plugin description.
     *
     * @param description the plugin description
     * @return the current instance of {@link Builder}
     */
    public Builder pluginDescription(String description) {
      descriptor.setPluginDescription(StringUtils.isNullOrEmpty(description) ? "" : description);
      return this;
    }

    /**
     * Sets the plugin class.
     *
     * @param pluginClass the plugin class
     * @return the current instance of {@link Builder}
     */
    public Builder pluginClass(String pluginClass) {
      if (StringUtils.isNotNullOrEmpty(pluginClass)) {
        descriptor.setPluginClass(pluginClass);
      }
      return this;
    }

    /**
     * Sets the plugin version.
     *
     * @param version the plugin version
     * @return the current instance of {@link Builder}
     */
    public Builder version(String version) {
      if (StringUtils.isNotNullOrEmpty(version)) {
        descriptor.setVersion(version);
      }
      return this;
    }

    /**
     * Sets the plugin provider.
     *
     * @param provider the plugin provider
     * @return the current instance of {@link Builder}
     */
    public Builder provider(String provider) {
      descriptor.setProvider(provider);
      return this;
    }

    /**
     * Sets the plugin dependencies.
     *
     * @param dependencies the plugin dependencies
     * @return the current instance of {@link Builder}
     */
    public Builder dependencies(String dependencies) {
      descriptor.setDependencies(dependencies);
      return this;
    }

    /**
     * Sets the plugin requires.
     *
     * @param requires the plugin requires
     * @return the current instance of {@link Builder}
     */
    public Builder requires(String requires) {
      if (StringUtils.isNotNullOrEmpty(requires)) {
        descriptor.setRequires(requires);
      }
      return this;
    }

    /**
     * Sets the plugin license.
     *
     * @param license the plugin license
     * @return the current instance of {@link Builder}
     */
    public Builder license(String license) {
      descriptor.setLicense(license);
      return this;
    }

    /**
     * Sets the plugin documentation.
     *
     * @param documentation the plugin documentation
     * @return the current instance of {@link Builder}
     */
    public Builder documentation(String documentation) {
      if (StringUtils.isNotNullOrEmpty(documentation)) {
        descriptor.setDocumentation(documentation);
      }
      return this;
    }

    /**
     * Sets the plugin metadata.
     *
     * @param metadata the metadata map
     * @return the current instance of {@link Builder}
     */
    public Builder metadata(Map<String, Object> metadata) {
      descriptor.setMetadata(metadata);
      return this;
    }

    /**
     * Sets the plugin properties.
     *
     * @param properties the properties map
     * @return the current instance of {@link Builder}
     */
    public Builder properties(Map<String, Object> properties) {
      descriptor.setProperties(properties);
      return this;
    }

    /**
     * Sets the plugin binary data.
     *
     * @param binaryData the binary data map
     * @return the current instance of {@link Builder}
     */
    public Builder binaryData(Map<String, Object> binaryData) {
      descriptor.setBinaryData(binaryData);
      return this;
    }

    /**
     * Builds the {@link DetailPluginDescriptor} instance.
     *
     * @return the built {@link DetailPluginDescriptor} instance
     */
    public DetailPluginDescriptor build() {
      return descriptor;
    }
  }

  /**
   * Creates a new {@link Builder} instance.
   *
   * @return a new {@link Builder} instance
   */
  public static Builder builder() {
    return new Builder();
  }


}
