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

package com.epam.ta.reportportal.core.integration.plugin.bootstrap;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.configs.Conditions;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
@Service
@Conditional(Conditions.NotTestCondition.class)
public class DefaultPluginBootstrapper implements PluginBootstrapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPluginBootstrapper.class);

  private final IntegrationTypeRepository integrationTypeRepository;
  private final PluginLoader pluginLoader;

  @Autowired
  public DefaultPluginBootstrapper(IntegrationTypeRepository integrationTypeRepository,
      PluginLoader pluginLoader) {
    this.integrationTypeRepository = integrationTypeRepository;
    this.pluginLoader = pluginLoader;
  }

  @Override
  @PostConstruct
  public void startUp() {
    // load and start all enabled plugins of application
    integrationTypeRepository.findAll()
        .stream()
        .filter(IntegrationType::isEnabled)
        .forEach(integrationType -> ofNullable(integrationType.getDetails()).ifPresent(
            integrationTypeDetails -> {
              try {
                pluginLoader.load(integrationType);
              } catch (Exception ex) {
                LOGGER.error("Unable to load plugin '{}'", integrationType.getName());
              }
            }));

  }

}