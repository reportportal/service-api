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

package com.epam.ta.reportportal.core.integration.plugin.info;

import com.epam.ta.reportportal.core.integration.plugin.file.PluginFileManager;
import com.epam.ta.reportportal.core.integration.plugin.validator.PluginInfoValidator;
import com.epam.ta.reportportal.core.integration.plugin.validator.exception.PluginValidationException;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.nio.file.Path;
import java.util.List;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link PluginInfoResolver} that retrieves and validates plugin
 * information.
 *
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
@Service
public class DefaultPluginInfoResolver implements PluginInfoResolver {

  private static final Logger logger = LoggerFactory.getLogger(DefaultPluginInfoResolver.class);


  private final PluginDescriptorFinder pluginDescriptorFinder;
  private final List<PluginInfoValidator> pluginInfoValidators;
  private final PluginFileManager pluginFileManager;

  @Autowired
  public DefaultPluginInfoResolver(PluginDescriptorFinder pluginDescriptorFinder,
      List<PluginInfoValidator> pluginInfoValidators,
      PluginFileManager pluginFileManager) {
    this.pluginDescriptorFinder = pluginDescriptorFinder;
    this.pluginInfoValidators = pluginInfoValidators;
    this.pluginFileManager = pluginFileManager;
  }

  @Override
  public PluginInfo resolveInfo(Path pluginPath) {
    try {
      PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginPath);
      final PluginInfo pluginInfo = new PluginInfo(pluginDescriptor.getPluginId(),
          pluginDescriptor.getVersion(), pluginPath);
      validateInfo(pluginInfo);
      return pluginInfo;
    } catch (PluginException e) {
      logger.error(e.getMessage(), e);
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
    }
  }


  private void validateInfo(PluginInfo pluginInfo) {
    pluginInfoValidators.forEach(v -> {
      try {
        v.validate(pluginInfo);
      } catch (PluginValidationException e) {
        pluginFileManager.delete(pluginInfo.getOriginalFilePath());
        throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
      }
    });
  }

}