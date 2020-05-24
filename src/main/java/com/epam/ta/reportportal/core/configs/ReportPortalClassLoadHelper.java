package com.epam.ta.reportportal.core.configs;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.ResourceLoaderClassLoadHelper;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ReportPortalClassLoadHelper extends ResourceLoaderClassLoadHelper {

	@Nullable
	private ResourceLoader resourceLoader;

	public ReportPortalClassLoadHelper() {
	}

	public ReportPortalClassLoadHelper(@Nullable ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (this.resourceLoader == null) {
			this.resourceLoader = SchedulerFactoryBean.getConfigTimeResourceLoader();
			if (this.resourceLoader == null) {
				this.resourceLoader = new DefaultResourceLoader();
			}
		}
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Assert.state(this.resourceLoader != null, "ResourceLoaderClassLoadHelper not initialized");
		return this.resourceLoader.getClassLoader().loadClass(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Class<? extends T> loadClass(String name, Class<T> clazz) throws ClassNotFoundException {
		return (Class<? extends T>) loadClass(name);
	}
}
