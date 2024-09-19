/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.model.validation;

import com.epam.reportportal.annotations.NotBlankWithSize;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
public class NotBlankWithSizeValidator implements ConstraintValidator<NotBlankWithSize, String> {

	private static final String NOT_NULL_PROPERTY = "{NotNull}";
	private static final String NOT_BLANK_PROPERTY = "{NotBlank}";
	private static final String SIZE_PROPERTY = "{Size}";
	private static final String SPACE = " ";

	private int min;
	private int max;

	@Override
	public void initialize(NotBlankWithSize notBlankWithSizeAnnotation) {
		this.min = notBlankWithSizeAnnotation.min();
		this.max = notBlankWithSizeAnnotation.max();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		boolean result;
		if (value == null) {
			result = false;

			setValidationMessage(NOT_NULL_PROPERTY, context);

		} else if (StringUtils.isBlank(value)) {
			result = false;

			String message = NOT_BLANK_PROPERTY + SPACE + SIZE_PROPERTY;
			setValidationMessage(message, context);

		} else if (value.length() < min || value.length() > max) {
			result = false;

			setValidationMessage(SIZE_PROPERTY, context);

		} else {
			result = true;
		} return result;
	}

	private void setValidationMessage(String message, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
	}
}
