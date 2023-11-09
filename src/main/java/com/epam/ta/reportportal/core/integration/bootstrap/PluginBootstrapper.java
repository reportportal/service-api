package com.epam.ta.reportportal.core.integration.bootstrap;

import javax.annotation.PostConstruct;

public interface PluginBootstrapper {
	@PostConstruct
	void startUp();
}
