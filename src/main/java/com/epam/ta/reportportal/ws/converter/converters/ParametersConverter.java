/*
 * Copyright 2018 EPAM Systems
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
package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.ws.model.ParameterResource;

import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ParametersConverter {

	private ParametersConverter() {
		//static only
	}

	public static final Function<ParameterResource, Parameter> TO_MODEL = resource -> {
		Parameter parameters = new Parameter();
		parameters.setKey(resource.getKey());
		parameters.setValue(resource.getValue());
		return parameters;
	};

	public static final Function<Parameter, ParameterResource> TO_RESOURCE = model -> {
		ParameterResource parameter = new ParameterResource();
		parameter.setKey(model.getKey());
		parameter.setValue(model.getValue());
		return parameter;
	};

}
