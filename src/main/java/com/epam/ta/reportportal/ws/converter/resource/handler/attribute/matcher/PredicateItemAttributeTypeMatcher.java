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

package com.epam.ta.reportportal.ws.converter.resource.handler.attribute.matcher;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.ItemAttributeType;

import java.util.function.Predicate;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PredicateItemAttributeTypeMatcher implements ItemAttributeTypeMatcher {

	private final Predicate<ItemAttribute> predicate;
	private final ItemAttributeType type;

	public PredicateItemAttributeTypeMatcher(Predicate<ItemAttribute> predicate, ItemAttributeType type) {
		this.predicate = predicate;
		this.type = type;
	}

	@Override
	public boolean matches(ItemAttribute attribute) {
		return predicate.test(attribute);
	}

	@Override
	public ItemAttributeType getType() {
		return type;
	}
}
