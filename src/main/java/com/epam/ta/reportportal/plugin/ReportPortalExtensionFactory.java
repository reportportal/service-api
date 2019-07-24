package com.epam.ta.reportportal.plugin;

import org.pf4j.DefaultExtensionFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ReportPortalExtensionFactory extends DefaultExtensionFactory {

	private final AutowireCapableBeanFactory autowireCapableBeanFactory;

	public ReportPortalExtensionFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
	}

	@Override
	public Object create(Class<?> extensionClass) {
		Object extension = super.create(extensionClass);
		if (null == extension) {
			return null;
		}
		this.autowireCapableBeanFactory.autowireBean(extension);
		return extension;
	}
}
