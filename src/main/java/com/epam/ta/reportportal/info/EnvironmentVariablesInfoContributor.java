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
		super(ImmutableMap.<String, Object>builder().put("environment",
				System.getenv()
						.entrySet()
						.stream()
						.filter(it -> it.getKey().startsWith(RP_ENV_PREFIX))
						.collect(Collectors.toMap(e -> e.getKey().replaceFirst(RP_ENV_PREFIX, "").toLowerCase(), Map.Entry::getValue))
		).build());
	}
}
