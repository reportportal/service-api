/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.auth.validation;

import com.epam.ta.reportportal.auth.store.entity.AbstractAuthConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract validation sequence provider
 *
 * @param <T> Type of Auth Config
 * @author Andrei Varabyeu
 */
public class AbstractAuthSequenceProvider<T extends AbstractAuthConfig> implements DefaultGroupSequenceProvider<T> {

	private final Class<T> modelType;

	public AbstractAuthSequenceProvider(Class<T> modelType) {
		this.modelType = modelType;
	}

	@Override
	public List<Class<?>> getValidationGroups(AbstractAuthConfig authConfig) {
		List<Class<?>> defaultGroupSequence = new ArrayList<>();
		defaultGroupSequence.add(modelType);

		if (authConfig != null && BooleanUtils.isTrue(authConfig.isEnabled())) {
			defaultGroupSequence.add(IfEnabled.class);
		}

		return defaultGroupSequence;
	}
}

