/*
 * Copyright 2023 EPAM Systems
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

import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginMetadata {

  private final PluginInfo pluginInfo;
  private final PluginPathInfo pluginPathInfo;

  private IntegrationGroupEnum integrationGroup;
  private Map<String, ?> pluginParams;

  public PluginMetadata(PluginInfo pluginInfo, PluginPathInfo pluginPathInfo) {
    this.pluginInfo = pluginInfo;
    this.pluginPathInfo = pluginPathInfo;
  }

  public PluginInfo getPluginInfo() {
    return pluginInfo;
  }

  public PluginPathInfo getPluginPathInfo() {
    return pluginPathInfo;
  }

  @Nullable
  public IntegrationGroupEnum getIntegrationGroup() {
    return integrationGroup;
  }

  public void setIntegrationGroup(IntegrationGroupEnum integrationGroup) {
    this.integrationGroup = integrationGroup;
  }

  @Nullable
  public Map<String, ?> getPluginParams() {
    return pluginParams;
  }

  public void setPluginParams(Map<String, ?> pluginParams) {
    this.pluginParams = pluginParams;
  }
}