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

import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;

/**
 * Custom validator for validating {@link WidgetRQ}, {@link WidgetPreviewRQ} objects<br>
 * This validator should be used for validating {@link WidgetRQ}, {@link WidgetPreviewRQ} objects<br>
 * before post operation in the {@link com.epam.ta.reportportal.ws.controller.IWidgetController}
 *
 * @author Aliaksei_Makayed
 */
public class WidgetRQCustomValidator implements SmartValidator {

	public static final String NOT_NULL = "NotNull";
	public static final String NAME = "name";
	public static final String CONTENT_PARAMETERS = "contentParameters";
	private static final List<Class<?>> SUPPORTED_CLASSES = ImmutableList.<Class<?>>builder().add(WidgetRQ.class, WidgetPreviewRQ.class)
			.build();

	@Autowired
	@Qualifier("validator")
	private Validator validator;

	@Override
	public boolean supports(Class<?> arg0) {
		return SUPPORTED_CLASSES.contains(arg0);
	}

	@Override
	public void validate(Object object, Errors errors, Object... validationHints) {
		if (isCheck(validationHints)) {
			ValidationUtils.rejectIfEmpty(errors, NAME, NOT_NULL, new Object[] { NAME });
			ValidationUtils.rejectIfEmpty(errors, CONTENT_PARAMETERS, NOT_NULL, new Object[] { CONTENT_PARAMETERS });
		}

		// validate all not empty fields
		validator.validate(object, errors);
	}

	@Override
	public void validate(Object target, Errors errors) {
		this.validate(target, errors, Object.class);
	}

	private boolean isCheck(Object... validationHints) {
		return validationHints != null && validationHints.length != 0 && validationHints[0].equals(WidgetRQCustomValidator.class);
	}
}