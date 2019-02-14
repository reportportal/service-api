package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ParametersConverterTest {

	@Test
	public void toResource() {
		final Parameter parameter = getParamener();
		final ParameterResource resource = ParametersConverter.TO_RESOURCE.apply(parameter);

		assertEquals(resource.getKey(), parameter.getKey());
		assertEquals(resource.getValue(), parameter.getValue());
	}

	@Test
	public void toModel() {
		final ParameterResource resource = getResource();
		final Parameter parameter = ParametersConverter.TO_MODEL.apply(resource);

		assertEquals(parameter.getKey(), resource.getKey());
		assertEquals(parameter.getValue(), resource.getValue());
	}

	private static Parameter getParamener() {
		Parameter parameter = new Parameter();
		parameter.setKey("key");
		parameter.setValue("value");
		return parameter;
	}

	private static ParameterResource getResource() {
		ParameterResource resource = new ParameterResource();
		resource.setKey("key");
		resource.setValue("value");
		return resource;
	}
}