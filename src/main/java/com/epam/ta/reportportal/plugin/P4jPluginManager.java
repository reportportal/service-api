package com.epam.ta.reportportal.plugin;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.util.concurrent.AbstractIdleService;
import org.pf4j.*;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.lang.Nullable;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class P4jPluginManager extends AbstractIdleService implements PluginBox {

	private final AutowireCapableBeanFactory context;
	private final String pluginsPath;
	private final org.pf4j.PluginManager pluginManager;

	public P4jPluginManager(String pluginsPath, AutowireCapableBeanFactory context,
			Collection<PluginDescriptorFinder> pluginDescriptorFinders) {
		this.context = context;
		this.pluginsPath = pluginsPath;
		pluginManager = new DefaultPluginManager(FileSystems.getDefault().getPath(this.pluginsPath)) {
			@Override
			protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {

				CompoundPluginDescriptorFinder compoundPluginDescriptorFinder = new CompoundPluginDescriptorFinder();

				pluginDescriptorFinders.forEach(compoundPluginDescriptorFinder::add);

				return
						// Demo is using the Manifest file
						// PropertiesPluginDescriptorFinder is commented out just to avoid error log
						//.add(new PropertiesPluginDescriptorFinder())
						compoundPluginDescriptorFinder;
			}

			@Override
			protected ExtensionFactory createExtensionFactory() {
				return new DefaultExtensionFactory() {
					@Override
					public Object create(Class<?> extensionClass) {
						Object obj = super.create(extensionClass);
						if (null == obj) {
							return null;
						}
						P4jPluginManager.this.context.autowireBean(obj);
						return obj;
					}
				};
			}
		};
	}

	@Override
	public List<Plugin> getPlugins() {
		return this.pluginManager.getPlugins()
				.stream()
				.flatMap(plugin -> pluginManager.getExtensionClasses(plugin.getPluginId())
						.stream()
						.map(ExtensionPoint::findByExtension)
						.filter(Optional::isPresent)
						.map(it -> new Plugin(plugin.getPluginId(), it.get())))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Plugin> getPlugin(String type) {
		return getPlugins().stream().filter(p -> p.getType().name().equalsIgnoreCase(type)).findAny();
	}

	public <T> Optional<T> getInstance(Class<T> extension) {
		return pluginManager.getExtensions(extension).stream().findFirst();
	}

	@Override
	public PluginState startUpPlugin(String pluginId) {

		PluginWrapper pluginWrapper = ofNullable(pluginManager.getPlugin(pluginId)).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Plugin not found: " + pluginId
		));

		return pluginManager.startPlugin(pluginId);
	}

	@Override
	@Nullable
	public String loadPlugin(Path path) {
		return pluginManager.loadPlugin(path);

		//		.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
		//				"Unable to start plugin: error during plugin uploading"
		//		)
	}

	@Override
	public boolean unloadPlugin(String pluginId) {
		return pluginManager.unloadPlugin(pluginId);
	}

	@Override
	public Optional<PluginWrapper> getPluginById(String id) {
		return ofNullable(pluginManager.getPlugin(id));
	}

	public <T> Optional<T> getInstance(String name, Class<T> extension) {
		return pluginManager.getExtensions(extension, name).stream().findFirst();
	}

	@Override
	protected void startUp() {
		// start and load all plugins of application
		pluginManager.loadPlugins();
		pluginManager.startPlugins();

	}

	@Override
	protected void shutDown() {
		// stop and unload all plugins
		pluginManager.stopPlugins();
		pluginManager.getPlugins().forEach(p -> pluginManager.unloadPlugin(p.getPluginId()));
	}
}
