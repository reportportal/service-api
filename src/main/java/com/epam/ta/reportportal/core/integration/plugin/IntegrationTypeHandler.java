package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.core.plugin.PluginMetadata;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface IntegrationTypeHandler {

  Optional<IntegrationType> getByName(String name);

  IntegrationType create(PluginMetadata pluginMetadata);

  IntegrationType update(IntegrationType integrationType, PluginMetadata pluginMetadata);
}