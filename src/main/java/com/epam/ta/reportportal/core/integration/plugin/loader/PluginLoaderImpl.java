package com.epam.ta.reportportal.core.integration.plugin.loader;

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.file.PluginFileManager;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginLoaderImpl implements PluginLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(PluginLoaderImpl.class);

	private final String pluginsDir;

	private final PluginFileManager pluginFileManager;
	private final Pf4jPluginBox pluginBox;

	@Autowired
	public PluginLoaderImpl(@Value("${rp.plugins.path}") String pluginsDir, PluginFileManager pluginFileManager, Pf4jPluginBox pluginBox) {
		this.pluginsDir = pluginsDir;
		this.pluginFileManager = pluginFileManager;
		this.pluginBox = pluginBox;
	}

	@Override
	public boolean load(IntegrationType integrationType) {
		if (pluginBox.getPluginById(integrationType.getName()).isPresent()) {
			return true;
		}
		return ofNullable(integrationType.getDetails()).map(IntegrationTypeDetails::getDetails).map(details -> {
			final String pluginId = integrationType.getName();
			final String fileName = getPropertyValue(pluginId, details, IntegrationTypeProperties.FILE_NAME);
			final Path pluginPath = Paths.get(pluginsDir, fileName);
			if (Files.notExists(pluginPath)) {
				loadPlugin(details, pluginId, fileName, pluginPath);
			}

			return pluginBox.loadPlugin(pluginPath).flatMap(pluginBox::getPluginById).map(pluginWrapper -> {
				if (PluginState.STARTED == pluginBox.startUpPlugin(pluginWrapper)) {
					final Optional<ExtensionPoint> extensionPoint = pluginBox.getInstance(pluginId, org.pf4j.ExtensionPoint.class);
					extensionPoint.ifPresent(extension -> LOGGER.info(Suppliers.formattedSupplier("Plugin - '{}' initialized.", pluginId)
							.get()));
					return true;
				} else {
					return false;
				}
			}).orElse(Boolean.FALSE);
		}).orElse(Boolean.FALSE);

	}

	private void loadPlugin(Map<String, Object> details, String pluginId, String fileName, Path pluginPath) {
		final String resourcesDir = getPropertyValue(pluginId, details, IntegrationTypeProperties.RESOURCES_DIRECTORY);
		final String fileId = getPropertyValue(pluginId, details, IntegrationTypeProperties.FILE_ID);
		final PluginPathInfo pluginPathInfo = new PluginPathInfo(pluginPath, Paths.get(resourcesDir), fileName, fileId);
		pluginFileManager.download(pluginPathInfo);
	}

	private String getPropertyValue(String pluginId, Map<String, Object> details, IntegrationTypeProperties property) {
		return property.getValue(details)
				.map(String::valueOf)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("'{}' property of the plugin - '{}' is not specified",
								property.name().toLowerCase(),
								pluginId
						).get()
				));
	}

	@Override
	public boolean unload(IntegrationType integrationType) {
		return pluginBox.getPluginById(integrationType.getName()).map(pluginBox::unloadPlugin).orElse(Boolean.TRUE);
	}
}
