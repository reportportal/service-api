/*
 * Copyright 2018 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
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

package com.epam.ta.reportportal.info;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.actuate.info.MapInfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collects provided environment variables with rp prefix.
 *
 * @author Pavel Bortnik
 */
@Component
public class EnvironmentVariablesInfoContributor extends MapInfoContributor {

	private static final String RP_ENV_PREFIX = "RP_ENVIRONMENT_VARIABLE_";

	public EnvironmentVariablesInfoContributor() {
		super(ImmutableMap.<String, Object>builder().put(
				"environment",
				System.getenv()
						.entrySet()
						.stream()
						.filter(it -> it.getKey().startsWith(RP_ENV_PREFIX))
						.collect(Collectors.toMap(e -> e.getKey().replaceFirst(RP_ENV_PREFIX, "").toLowerCase(), Map.Entry::getValue))
		).build());
	}
}
