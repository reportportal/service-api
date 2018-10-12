package com.epam.ta.reportportal.plugin;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.google.common.util.concurrent.AbstractIdleService;
import org.pf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.Optional;

public class P4jPluginManager extends AbstractIdleService implements PluginBox {

	@Autowired
	private AutowireCapableBeanFactory context;
	private final org.pf4j.PluginManager pluginManager;


	public P4jPluginManager() {
		pluginManager = new DefaultPluginManager(FileSystems.getDefault()
				.getPath("/Users/andrei_varabyeu/work/sources/reportportal/plugin-bts-jira/build/plugins")) {
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
						context.autowireBean(obj);
						return obj;
					}
				};
			}
		};
	}

	@Override
	public List<Plugin> getPlugins() {
		return null;
	}

	@Override
	public Optional<Plugin> getPlugin(String type) {
		return Optional.empty();
	}

	@Override
	protected void startUp() {
		// start and load all plugins of application
		pluginManager.loadPlugins();
		pluginManager.startPlugins();

		System.out.println(pluginManager.getExtensions(BtsExtension.class).get(0));
		System.out.println(pluginManager.getExtensions("jira-bts").get(0));

	}

	@Override
	protected void shutDown() {
		// stop and unload all plugins
		pluginManager.stopPlugins();
		pluginManager.getPlugins().forEach(p -> pluginManager.unloadPlugin(p.getPluginId()));
	}
}
