package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ItemAttributeConverterTest {

	@Test
	void fromResource() {
		ItemAttributeResource resource = new ItemAttributeResource("key", "val");
		final ItemAttribute itemAttribute = ItemAttributeConverter.FROM_RESOURCE.apply(resource);

		assertEquals(itemAttribute.getKey(), resource.getKey());
		assertEquals(itemAttribute.getValue(), resource.getValue());
		assertEquals(itemAttribute.isSystem(), false);
	}
}