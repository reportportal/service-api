/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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