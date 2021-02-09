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

package com.epam.ta.reportportal.core.item.impl.provider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public enum DataProviderType {

	WIDGET_BASED("widget"),
	LAUNCH_BASED("launch"),
	FILTER_BASED("filter");

	private final String type;

	DataProviderType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public static Optional<DataProviderType> findByName(@Nullable String name) {
		return Arrays.stream(DataProviderType.values()).filter(type -> type.getType().equalsIgnoreCase(name)).findAny();
	}
}
