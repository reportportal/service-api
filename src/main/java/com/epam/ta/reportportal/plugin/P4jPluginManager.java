package com.epam.ta.reportportal.plugin;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.google.common.util.concurrent.AbstractIdleService;
import org.pf4j.*;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class P4jPluginManager extends AbstractIdleService implements PluginBox {

	private final AutowireCapableBeanFactory context;
	private final String pluginsPath;
	private final org.pf4j.PluginManager pluginManager;

	public P4jPluginManager(String pluginsPath, AutowireCapableBeanFactory context) {
		this.context = context;
		this.pluginsPath = pluginsPath;
		pluginManager = new DefaultPluginManager(FileSystems.getDefault().getPath(this.pluginsPath)) {
			@Override
			protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {
				return new CompoundPluginDescriptorFinder()
						// Demo is using the Manifest file
						// PropertiesPluginDescriptorFinder is commented out just to avoid error log
						//.add(new PropertiesPluginDescriptorFinder())
						.add(new ManifestPluginDescriptorFinder());
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
