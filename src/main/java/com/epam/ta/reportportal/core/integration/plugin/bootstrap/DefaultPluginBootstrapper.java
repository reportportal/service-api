package com.epam.ta.reportportal.core.integration.plugin.bootstrap;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
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