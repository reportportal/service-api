package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.plugin.P4jPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginConfiguration {

	@Autowired
	private AutowireCapableBeanFactory context;

	@Autowired
	@Value("${rp.plugins.path}")
	private String pluginsPath;

	@Bean
	public PluginBox p4jPluginBox() {
		P4jPluginManager manager = new P4jPluginManager(pluginsPath, context);
		manager.startAsync();
		return manager;
	}
}
