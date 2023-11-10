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

package com.epam.ta.reportportal.core.integration.plugin.validator;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.validator.exception.PluginValidationException;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.Optional;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Validator that verifies plugin extension class.
 *
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
@Service
public class ExtensionClassesValidator implements PluginInfoValidator {

  private final PluginManager validationBox;

  @Autowired
  public ExtensionClassesValidator(PluginManager validationBox) {
    this.validationBox = validationBox;
  }

  @Override
  public void validate(PluginInfo pluginInfo) throws PluginValidationException {

    final String pluginId = ofNullable(
        validationBox.loadPlugin(pluginInfo.getOriginalFilePath())).orElseThrow(() -> {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
          Suppliers.formattedSupplier("Failed to load plugin into validation box from file = '{}'",
              pluginInfo.getOriginalFilePath().getFileName().toString()
          ).get()
      );
    });

    validationBox.startPlugin(pluginId);
    final PluginWrapper plugin = validationBox.getPlugin(pluginId);

    final boolean validExtension = plugin.getPluginManager()
        .getExtensionClasses(plugin.getPluginId())
        .stream()
        .map(ExtensionPoint::findByExtension)
        .anyMatch(Optional::isPresent);

    validationBox.unloadPlugin(pluginId);

    if (!validExtension) {
      throw new PluginValidationException(Suppliers.formattedSupplier(
          "New plugin with id = '{}' doesn't have mandatory extension classes.",
          pluginId
      ).get());
    }

  }

}