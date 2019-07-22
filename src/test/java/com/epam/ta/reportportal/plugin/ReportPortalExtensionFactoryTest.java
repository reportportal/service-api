package com.epam.ta.reportportal.plugin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ReportPortalExtensionFactoryTest {

	private final AutowireCapableBeanFactory autowireCapableBeanFactory = mock(AutowireCapableBeanFactory.class);

	private final ReportPortalExtensionFactory extensionFactory = new ReportPortalExtensionFactory(autowireCapableBeanFactory);
	private final ReportPortalExtensionFactory mockedFactory = mock(ReportPortalExtensionFactory.class);

	@Test
	void createPositive() {

		Object object = extensionFactory.create(Object.class);
		verify(autowireCapableBeanFactory, times(1)).autowireBean(object);
	}

	@Test
	void createNegative() {

		when(mockedFactory.create(any())).thenReturn(null);
		Object object = mockedFactory.create(Object.class);
		verify(autowireCapableBeanFactory, times(0)).autowireBean(object);
	}
}