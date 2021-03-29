package com.epam.ta.reportportal.core.plugin;

import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;

import javax.annotation.Nullable;
import java.util.Map;

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
