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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Collects provided environment variables with rp prefix.
 *
 * @author Pavel Bortnik
 */
@Component
public class EnvironmentVariablesInfoContributor implements InfoContributor {

	private static final String RP_ENV_PREFIX = "rp.environment.";

	@Autowired
	private ConfigurableEnvironment environment;

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetails(ImmutableMap.<String, Object>builder().put("environment", resolveProperties()).build());
	}

	private Map<String, Object> resolveProperties() {

		List<MapPropertySource> propertySources = new ArrayList<>();

		environment.getPropertySources().forEach(it -> {
			if (it instanceof MapPropertySource) {
				propertySources.add((MapPropertySource) it);
			}
		});

		return propertySources.stream()
				.map(propertySource -> propertySource.getSource().keySet())
				.flatMap(Collection::stream)
				.filter(it -> it.startsWith(RP_ENV_PREFIX))
				.distinct()
				.collect(HashMap::new, (m, it) -> m.put(it, environment.getProperty(it)), HashMap::putAll);

	}

}
