/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Shows list of supported extensions providers.
 *
 * @author Pavel Bortnik
 */

@Component
public class ExtensionsInfoContributor implements ExtensionContributor {

	private static final String EXTENSION_KEY = "extension";

	private static final String BUGTRACKING_KEY = "bugtracking";

	private final DiscoveryClient discoveryClient;

	@Autowired
	public ExtensionsInfoContributor(DiscoveryClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}

	@Override
	public Map<String, ?> contribute() {
		Set<String> collect = discoveryClient.getServices()
				.stream()
				.flatMap(service -> discoveryClient.getInstances(service).stream())
				.filter(instance -> instance.getMetadata().containsKey(EXTENSION_KEY))
				.map(instance -> instance.getMetadata().get(EXTENSION_KEY))
				.collect(Collectors.toCollection(TreeSet::new));
		return ImmutableMap.<String, Object>builder().put(BUGTRACKING_KEY, collect).build();
	}
}
