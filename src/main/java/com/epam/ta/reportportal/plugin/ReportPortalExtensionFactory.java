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
