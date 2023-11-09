package com.epam.ta.reportportal.core.integration.plugin.info;

import com.epam.ta.reportportal.core.plugin.PluginInfo;

import java.nio.file.Path;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginInfoResolver {

	PluginInfo resolveInfo(Path pluginPath);
}