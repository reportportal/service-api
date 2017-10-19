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

package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.BaseTest;
import com.fasterxml.jackson.annotation.JsonView;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Validates Jackson views behaviour
 *
 * @author Andrei Varabyeu
 */
public class JacksonViewAwareSerializationTest extends BaseTest {
	private static final String FIELD_WITH_VIEW = "field with view";
	private static final String NO_VIEW_THERE = "no view there";

	private static DemoBean bean;

	@Autowired
	private MappingJackson2HttpMessageConverter converter;

	@BeforeClass
	public static void prepareBean() {
		bean = new DemoBean();
		bean.setFieldWithNoView(NO_VIEW_THERE);
		bean.setFieldWithView(FIELD_WITH_VIEW);
	}

	@Test
	public void testWithView() throws HttpMessageNotWritableException, IOException {

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(new JacksonViewAware(bean, DemoViewFirst.class), MediaType.APPLICATION_JSON, outputMessage);

		Assert.assertThat(outputMessage.getBodyAsString(), not(containsString(NO_VIEW_THERE)));
		Assert.assertThat(outputMessage.getBodyAsString(), containsString(FIELD_WITH_VIEW));
	}

	@Test
	public void testWithNoView() throws HttpMessageNotWritableException, IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(bean, MediaType.APPLICATION_JSON, outputMessage);
		Assert.assertThat(outputMessage.getBodyAsString(), containsString(NO_VIEW_THERE));
		Assert.assertThat(outputMessage.getBodyAsString(), containsString(FIELD_WITH_VIEW));
	}

	private static class DemoViewFirst {
	}

	private static class DemoViewSecond {
	}

	public static class DemoBean {

		@JsonView({ DemoViewFirst.class, DemoViewSecond.class })
		private String fieldWithView;

		@JsonView(DemoViewSecond.class)
		private String fieldWithNoView;

		public void setFieldWithNoView(String fieldWithNoView) {
			this.fieldWithNoView = fieldWithNoView;
		}

		public void setFieldWithView(String fieldWithView) {
			this.fieldWithView = fieldWithView;
		}

		public String getFieldWithNoView() {
			return fieldWithNoView;
		}

		public String getFieldWithView() {
			return fieldWithView;
		}
	}
}