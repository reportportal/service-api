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
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.ItemAttributesRQ;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemAttributeConverter {

	public static final int MAX_ATTRIBUTE_LENGTH = 512;

	private ItemAttributeConverter() {
		//static only
	}

	public static final Function<ItemAttributeResource, ItemAttribute> FROM_RESOURCE = it -> {
		ItemAttribute itemAttribute = new ItemAttribute();

		String key = it.getKey();
		if (key != null && key.length() > MAX_ATTRIBUTE_LENGTH){
			key = key.substring(0, MAX_ATTRIBUTE_LENGTH);
		}
		String value = it.getValue();
		if (value != null && value.length() > MAX_ATTRIBUTE_LENGTH){
			value = value.substring(0, MAX_ATTRIBUTE_LENGTH);
		}
		itemAttribute.setKey(key);
		itemAttribute.setValue(value);

		if (it instanceof ItemAttributesRQ itemAttributesRQ) {
			itemAttribute.setSystem(itemAttributesRQ.isSystem());
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
