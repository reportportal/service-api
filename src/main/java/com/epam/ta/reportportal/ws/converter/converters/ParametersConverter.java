/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.item.Parameter;
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
