/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.utils.item.updater;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.ta.reportportal.ws.model.TestItemResource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class RetriesResourceUpdater implements ResourceUpdater<TestItemResource> {

	private final Map<Long, List<TestItem>> retriesMapping;

	private RetriesResourceUpdater(Map<Long, List<TestItem>> retriesMapping) {
		this.retriesMapping = retriesMapping;
	}

	@Override
	public void updateResource(TestItemResource resource) {
		ofNullable(retriesMapping.get(resource.getItemId())).ifPresent(retries -> resource.setRetries(retries.stream()
				.map(TestItemConverter.TO_RESOURCE)
				.collect(Collectors.toList())));
	}

	public static RetriesResourceUpdater of(Map<Long, List<TestItem>> retriesMapping) {
		return new RetriesResourceUpdater(retriesMapping);
	}
}
