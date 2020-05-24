package com.epam.ta.reportportal.core.configs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ReportPortalClassLoadHelperTest {

	private ResourceLoader resourceLoader = mock(ResourceLoader.class);
	private ReportPortalClassLoadHelper classLoadHelper;

	@Test
	void initializeTest() {
		classLoadHelper = new ReportPortalClassLoadHelper();
		classLoadHelper.initialize();
		Assertions.assertNotNull(classLoadHelper.getClassLoader());
	}

	@Test
	void loadClassTest() throws ClassNotFoundException {
		classLoadHelper = new ReportPortalClassLoadHelper(resourceLoader);
		when(resourceLoader.getClassLoader()).thenReturn(this.getClass().getClassLoader());
		Class<?> clazz = classLoadHelper.loadClass(this.getClass().getCanonicalName());
		Assertions.assertEquals(this.getClass(), clazz);
	}
}