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

package com.epam.ta.reportportal.core.configs.resource;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.*;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.launch.LaunchResourceAttributeLogger;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.launch.LaunchResourceAttributeUpdater;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.launch.LaunchResourceMetadataAttributeUpdater;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.matcher.ItemAttributeTypeMatcher;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.matcher.PredicateItemAttributeTypeMatcher;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.resolver.ItemAttributeTypeResolver;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.resolver.ItemAttributeTypeResolverDelegate;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.core.launch.cluster.ClusterGeneratorImpl.RP_CLUSTER_LAST_RUN_KEY;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class ResourceAttributeHandlerConfig {

	@Bean
	public ResourceAttributeHandler<LaunchResource> launchResourceAttributeUpdater() {
		return new LaunchResourceAttributeUpdater();
	}

	@Bean
	public ResourceAttributeHandler<LaunchResource> launchResourceMetadataAttributeUpdater() {
		return new LaunchResourceMetadataAttributeUpdater(Set.of(RP_CLUSTER_LAST_RUN_KEY));
	}

	@Bean
	public ResourceAttributeHandler<LaunchResource> unresolvedAttributesLaunchLogger() {
		return new LaunchResourceAttributeLogger("Attributes with unresolved type: ");
	}

	@Bean
	public ItemAttributeTypeMatcher systemAttributeTypePredicateMatcher() {
		return new PredicateItemAttributeTypeMatcher(ItemAttribute::isSystem, ItemAttributeType.SYSTEM);
	}

	@Bean
	public ItemAttributeTypeMatcher publicAttributeTypePredicateMatcher() {
		return new PredicateItemAttributeTypeMatcher(it -> !it.isSystem(), ItemAttributeType.PUBLIC);
	}

	@Bean
	public ItemAttributeTypeResolver itemAttributeTypeResolver() {
		return new ItemAttributeTypeResolverDelegate(List.of(publicAttributeTypePredicateMatcher(), systemAttributeTypePredicateMatcher()));
	}

	@Bean
	public Map<ItemAttributeType, ResourceAttributeHandler<LaunchResource>> attributeUpdaterMapping() {
		return ImmutableMap.<ItemAttributeType, ResourceAttributeHandler<LaunchResource>>builder()
				.put(ItemAttributeType.PUBLIC, launchResourceAttributeUpdater())
				.put(ItemAttributeType.SYSTEM, launchResourceMetadataAttributeUpdater())
				.put(ItemAttributeType.UNRESOLVED, unresolvedAttributesLaunchLogger())
				.build();
	}
}
