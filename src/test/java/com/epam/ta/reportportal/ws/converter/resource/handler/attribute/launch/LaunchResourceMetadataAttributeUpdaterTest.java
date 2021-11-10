/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.resource.handler.attribute.launch;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.core.launch.cluster.pipeline.SaveLastRunAttributePartProvider.RP_CLUSTER_LAST_RUN_KEY;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchResourceMetadataAttributeUpdaterTest {

	private final LaunchResourceMetadataAttributeUpdater updater = new LaunchResourceMetadataAttributeUpdater(Set.of(RP_CLUSTER_LAST_RUN_KEY));

	@Test
	void shouldUpdateMetadataWhenAttributeMatches() {
		final LaunchResource launchResource = new LaunchResource();
		final List<ItemAttribute> attributes = List.of(new ItemAttribute(RP_CLUSTER_LAST_RUN_KEY, "v1", false),
				new ItemAttribute("k2", "v2", false)
		);
		updater.handle(launchResource, attributes);

		final Map<String, Object> metadata = launchResource.getMetadata();
		Assertions.assertFalse(metadata.isEmpty());
		Assertions.assertEquals("v1", metadata.get(RP_CLUSTER_LAST_RUN_KEY));
	}

	@Test
	void shouldNotUpdateMetadataWhenAttributeMatches() {
		final LaunchResource launchResource = new LaunchResource();
		final List<ItemAttribute> attributes = List.of(new ItemAttribute("k1", "v1", false),
				new ItemAttribute("k2", "v2", false)
		);
		updater.handle(launchResource, attributes);

		Assertions.assertTrue(MapUtils.isEmpty(launchResource.getMetadata()));
	}

}