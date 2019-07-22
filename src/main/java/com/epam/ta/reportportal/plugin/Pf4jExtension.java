package com.epam.ta.reportportal.plugin;

import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginDescriptorFinder;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class Pf4jExtension extends DefaultPluginManager {

	private final ExtensionFactory extensionFactory;
	private final Collection<PluginDescriptorFinder> pluginDescriptorFinders;

	public Pf4jExtension(String pluginsRoot, ExtensionFactory extensionFactory, Collection<PluginDescriptorFinder> pluginDescriptorFinders) {
		super(FileSystems.getDefault().getPath(pluginsRoot));
		this.extensionFactory = extensionFactory;
		this.pluginDescriptorFinders = pluginDescriptorFinders;
	}

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
		return extensionFactory;
	}

	@Override
	public Path getPluginsRoot() {
		Path pluginsRoot = super.getPluginsRoot();
		return pluginsRoot.normalize();
	}
}

