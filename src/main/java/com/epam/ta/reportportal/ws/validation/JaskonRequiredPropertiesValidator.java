/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.ws.validation;

import com.epam.ta.reportportal.commons.accessible.Accessible;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JaskonRequiredPropertiesValidator implements Validator {
	private static final Logger LOGGER = LoggerFactory.getLogger(JaskonRequiredPropertiesValidator.class);

	@Override
	public boolean supports(Class<?> clazz) {
		return AnnotationUtils.isAnnotationDeclaredLocally(JsonInclude.class, clazz);
	}

	@Override
	public void validate(Object object, Errors errors) {
		for (Field field : collectFields(object.getClass())) {
			if (AnnotationUtils.isAnnotationDeclaredLocally(JsonInclude.class, field.getType())) {
				try {
					Object innerObject = Accessible.on(object).field(field).getValue();
					if (null != innerObject) {
						errors.pushNestedPath(field.getName());
						validate(innerObject, errors);
					}
				} catch (Exception e) {
					LOGGER.error("JaskonRequiredPropertiesValidator error: " + e.getMessage(), e);
					// do nothing
				}

			}
			if (field.isAnnotationPresent(JsonProperty.class) && field.getAnnotation(JsonProperty.class).required()) {
				String errorCode = new StringBuilder("NotNull.").append(field.getName()).toString();
				ValidationUtils.rejectIfEmpty(errors, field.getName(), errorCode, new Object[] { errorCode });
			}
		}
		if (errors.getNestedPath() != null && errors.getNestedPath().length() != 0) {
			errors.popNestedPath();
		}
	}

	private List<Field> collectFields(Class<?> clazz) {
		List<Field> fields = null;
		if (!Object.class.equals(clazz.getSuperclass())) {
			fields = collectFields(clazz.getSuperclass());
		}

		fields = (fields == null) ? new ArrayList<>() : fields;
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return fields;
	}
}