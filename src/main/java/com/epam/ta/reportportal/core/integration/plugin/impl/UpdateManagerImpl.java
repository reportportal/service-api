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
package com.epam.ta.reportportal.core.integration.plugin.impl;

import java.nio.file.Path;
import java.util.List;
import org.pf4j.PluginManager;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.pf4j.update.UpdateManager;
import org.pf4j.update.UpdateRepository;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class UpdateManagerImpl extends UpdateManager {

  public UpdateManagerImpl(PluginManager pluginManager,
      List<UpdateRepository> repos) {
    super(pluginManager, repos);
  }

  @Override
  public Path downloadPlugin(String id, String version) {
    return super.downloadPlugin(id, version);
  }

  @Override
  public PluginRelease findReleaseForPlugin(String id, String version) {
    return super.findReleaseForPlugin(id, version);
  }
}
