/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ItemInfoUtilsTest {

	@Test
	void nullAttributesCollectionTest() {
		Optional<ItemAttribute> attribute = ItemInfoUtils.extractAttribute(null, "key");
		assertTrue(attribute.isEmpty());
	}

	@Test
	void emptyAttributesCollectionTest() {
		Optional<ItemAttribute> attribute = ItemInfoUtils.extractAttribute(Collections.emptyList(), "key");
		assertTrue(attribute.isEmpty());
	}

	@Test
	void shouldFindNonSystemAttribute() {
		String key = "key1";
		Optional<ItemAttribute> attribute = ItemInfoUtils.extractAttribute(getAttributes(), key);
		assertTrue(attribute.isPresent());
		assertEquals(key, attribute.get().getKey());
	}

	@Test
	void shouldFindSystemAttribute() {
		String key = "key3";
		Optional<ItemAttribute> attribute = ItemInfoUtils.extractAttribute(getAttributes(), key);
		assertTrue(attribute.isPresent());
		assertEquals(key, attribute.get().getKey());
	}

	@Test
	void shouldNotFindAttribute() {
		String key = "not-exist";
		Optional<ItemAttribute> attribute = ItemInfoUtils.extractAttribute(getAttributes(), key);
		assertTrue(attribute.isEmpty());
	}

	@Test
	void nullAttributeResourceCollectionTest() {
		Optional<ItemAttributeResource> itemAttributeResource = ItemInfoUtils.extractAttributeResource(null, "key");
		assertTrue(itemAttributeResource.isEmpty());
	}

	@Test
	void emptyAttributeResourcesCollectionTest() {
		Optional<ItemAttributeResource> itemAttributeResource = ItemInfoUtils.extractAttributeResource(Collections.emptyList(), "key");
		assertTrue(itemAttributeResource.isEmpty());
	}

	@Test
	void shouldFindAttributeResource() {
		String key = "key1";
		Optional<ItemAttributeResource> itemAttributeResource = ItemInfoUtils.extractAttributeResource(getAttributeResources(), key);
		assertTrue(itemAttributeResource.isPresent());
		assertEquals(key, itemAttributeResource.get().getKey());
	}

	@Test
	void shouldNotFindAttributeResource() {
		String key = "not-exist";
		Optional<ItemAttributeResource> itemAttributeResource = ItemInfoUtils.extractAttributeResource(getAttributeResources(), key);
		assertTrue(itemAttributeResource.isEmpty());
	}

	private List<ItemAttribute> getAttributes() {
		return Lists.newArrayList(
				new ItemAttribute("key1", "value1", false),
				new ItemAttribute("key2", "value2", false),
				new ItemAttribute("key3", "value3", true)
		);
	}

	private List<ItemAttributeResource> getAttributeResources() {
		return Lists.newArrayList(new ItemAttributeResource("key1", "value1"), new ItemAttributeResource("key2", "value2"));
	}
}