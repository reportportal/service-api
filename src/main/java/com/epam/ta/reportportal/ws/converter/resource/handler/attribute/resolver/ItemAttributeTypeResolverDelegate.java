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

package com.epam.ta.reportportal.ws.converter.resource.handler.attribute.resolver;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.ItemAttributeType;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.matcher.ItemAttributeTypeMatcher;

import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ItemAttributeTypeResolverDelegate implements ItemAttributeTypeResolver {

	private final List<ItemAttributeTypeMatcher> matchers;

	public ItemAttributeTypeResolverDelegate(List<ItemAttributeTypeMatcher> matchers) {
		this.matchers = matchers;
	}

	@Override
	public Optional<ItemAttributeType> resolve(ItemAttribute attribute) {
		return matchers.stream().filter(m -> m.matches(attribute)).findFirst().map(ItemAttributeTypeMatcher::getType);
	}
}
