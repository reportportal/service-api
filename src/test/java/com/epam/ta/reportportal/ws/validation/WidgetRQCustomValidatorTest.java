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

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class WidgetRQCustomValidatorTest extends BaseTest {

	@Autowired
	private WidgetRQCustomValidator validator;

	@Test
	public void testInnerFilelds() {
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setFilterId("1234");
		widgetRQ.setContentParameters(new ContentParameters());
		widgetRQ.setName("name");
		Errors errors = new BeanPropertyBindingResult(widgetRQ, "widgetRQ");
		validator.validate(widgetRQ, errors);
		List<ObjectError> errorsList = errors.getAllErrors();
		Assert.assertFalse(errorsList.isEmpty());
		Assert.assertThat(errors.getFieldError("contentParameters.type"), not(nullValue()));
		Assert.assertThat(errors.getFieldError("contentParameters.gadget"), not(nullValue()));
	}

	@Test
	public void tesRequiredFields() {
		WidgetRQ widgetRQ = new WidgetRQ();
		Errors errors = new BeanPropertyBindingResult(widgetRQ, "widgetRQ");
		validator.validate(widgetRQ, errors, WidgetRQCustomValidator.class);
		List<ObjectError> errorsList = errors.getAllErrors();
		Assert.assertFalse(errorsList.isEmpty());
		Assert.assertThat(errors.getFieldError("contentParameters"), not(nullValue()));
		Assert.assertThat(errors.getFieldError("name"), not(nullValue()));
	}

	@Test
	public void positiveTest() {
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setFilterId("1234");
		widgetRQ.setName("name");
		ContentParameters contententParameters = new ContentParameters();
		contententParameters.setType("line_chart");
		contententParameters.setGadget("old_line_chart");
		contententParameters.setItemsCount(50);
		List<String> fields = new ArrayList<>();
		fields.add("name");
		contententParameters.setContentFields(fields);
		contententParameters.setMetadataFields(fields);
		widgetRQ.setContentParameters(contententParameters);
		Errors errors = new BeanPropertyBindingResult(widgetRQ, "widgetRQ");
		validator.validate(widgetRQ, errors);
		Assert.assertTrue(errors.getAllErrors().isEmpty());
	}

	@Ignore
	@Test
	public void testIncorrectType() {
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setFilterId("12344");
		widgetRQ.setName("name1");
		ContentParameters contententParameters = new ContentParameters();
		contententParameters.setType("line_chartline_chart");
		contententParameters.setGadget("old_line_chart");
		List<String> fields = new ArrayList<>();
		fields.add("name");
		contententParameters.setContentFields(fields);
		contententParameters.setMetadataFields(fields);
		widgetRQ.setContentParameters(contententParameters);
		Errors errors = new BeanPropertyBindingResult(widgetRQ, "widgetRQ");
		validator.validate(widgetRQ, errors);
		Assert.assertFalse(errors.getAllErrors().isEmpty());
		Assert.assertThat(errors.getFieldError("Pattern.contentParameters.type"), not(nullValue()));
	}
}