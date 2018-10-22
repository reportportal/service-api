package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.plugin.P4jPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginConfiguration {

	@Bean
	public PluginBox p4jPluginBox(){
		P4jPluginManager manager = new P4jPluginManager();
		manager.startAsync();
		return manager;
	}
}
