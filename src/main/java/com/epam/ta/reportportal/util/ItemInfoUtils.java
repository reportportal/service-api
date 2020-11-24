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

package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.attribute.UpdateItemAttributeRQ;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.util.Predicates.ITEM_ATTRIBUTE_EQUIVALENCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 * <p>
 * Util class. Contains methods for updating
 * {@link com.epam.ta.reportportal.entity.launch.Launch}/{@link com.epam.ta.reportportal.entity.item.TestItem} attributes and description.
 */
public class ItemInfoUtils {

	private ItemInfoUtils() {
		//static only
	}

	public static Optional<String> updateDescription(BulkInfoUpdateRQ.Description descriptionRq, String existDescription) {
		if (!Objects.isNull(descriptionRq) && !Objects.isNull(descriptionRq.getComment())) {
			switch (descriptionRq.getAction()) {
				case UPDATE: {
					return Optional.of(existDescription + " " + descriptionRq.getComment());
				}
				case CREATE: {
					return Optional.of(descriptionRq.getComment());
				}
				default: {
					return Optional.empty();
				}
			}
		} else {
			return Optional.empty();
		}
	}

	public static ItemAttribute findAttributeByResource(Set<ItemAttribute> attributes, ItemAttributeResource resource) {
		return attributes.stream()
				.filter(attr -> ITEM_ATTRIBUTE_EQUIVALENCE.test(attr, resource))
				.findAny()
				.orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST, "Cannot delete not common attribute"));
	}

	public static void updateAttribute(Set<ItemAttribute> attributes, UpdateItemAttributeRQ updateItemAttributeRQ) {
		ItemAttribute itemAttribute = attributes.stream()
				.filter(attr -> ITEM_ATTRIBUTE_EQUIVALENCE.test(attr, updateItemAttributeRQ.getFrom()))
				.findAny()
				.orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST, "Cannot update not common attribute"));
		attributes.remove(itemAttribute);
		itemAttribute.setKey(updateItemAttributeRQ.getTo().getKey());
		itemAttribute.setValue(updateItemAttributeRQ.getTo().getValue());
		attributes.add(itemAttribute);
	}

	public static boolean containsAttribute(Set<ItemAttribute> attributes, ItemAttributeResource resource) {
		return attributes.stream().noneMatch(attr -> ITEM_ATTRIBUTE_EQUIVALENCE.test(attr, resource));
	}

	public static Optional<ItemAttribute> extractAttribute(Collection<ItemAttribute> collection, String key) {
		return CollectionUtils.isEmpty(collection) ?
				Optional.empty() :
				collection.stream().filter(it -> key.equalsIgnoreCase(it.getKey())).findAny();
	}

	public static Optional<ItemAttributeResource> extractAttributeResource(Collection<ItemAttributeResource> collection, String key) {
		return CollectionUtils.isEmpty(collection) ?
				Optional.empty() :
				collection.stream().filter(it -> key.equalsIgnoreCase(it.getKey())).findAny();
	}

}
