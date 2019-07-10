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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemAttributeConverter {

	private ItemAttributeConverter() {
		//static only
	}

	public static final Function<ItemAttributeResource, ItemAttribute> FROM_RESOURCE = it -> {
		ItemAttribute itemAttribute = new ItemAttribute();
		itemAttribute.setKey(it.getKey());
		itemAttribute.setValue(it.getValue());
		if (it instanceof ItemAttributesRQ) {
			itemAttribute.setSystem(((ItemAttributesRQ) it).isSystem());
		} else {
			itemAttribute.setSystem(false);
		}
		return itemAttribute;
	};

	public static final BiFunction<ItemAttributesRQ, Launch, ItemAttribute> TO_LAUNCH_ATTRIBUTE = (model, launch) -> {
		ItemAttribute itemAttribute = new ItemAttribute(model.getKey(), model.getValue(), model.isSystem());
		itemAttribute.setLaunch(launch);
		return itemAttribute;
	};

	public static final BiFunction<ItemAttributesRQ, TestItem, ItemAttribute> TO_TEST_ITEM_ATTRIBUTE = (model, item) -> {
		ItemAttribute itemAttribute = new ItemAttribute(model.getKey(), model.getValue(), model.isSystem());
		itemAttribute.setTestItem(item);
		return itemAttribute;
	};
}