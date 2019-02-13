package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemAttributeConverterTest {

	@Test
	public void fromResource() {
		ItemAttributeResource resource = new ItemAttributeResource("key", "val", false);
		final ItemAttribute itemAttribute = ItemAttributeConverter.FROM_RESOURCE.apply(resource);

		assertEquals(itemAttribute.getKey(), resource.getKey());
		assertEquals(itemAttribute.getValue(), resource.getValue());
		assertEquals(itemAttribute.isSystem(), resource.isSystem());
	}
}