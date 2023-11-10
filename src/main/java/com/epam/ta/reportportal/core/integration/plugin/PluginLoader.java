package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.entity.integration.IntegrationType;
import org.pf4j.PluginWrapper;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginLoader {

  /**
   * Load plugin to the plugin manager using plugin data
   *
   * @param integrationType {@link IntegrationType}
   * @return {@link PluginWrapper#getPluginId()}
   */
  boolean load(IntegrationType integrationType);

  /**
   * Unload plugin from the plugin manager using plugin data
   *
   * @param integrationType {@link IntegrationType}
   * @return {@link PluginWrapper#getPluginId()}
   */
  boolean unload(IntegrationType integrationType);
}