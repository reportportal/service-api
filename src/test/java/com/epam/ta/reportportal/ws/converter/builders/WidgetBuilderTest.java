/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class WidgetBuilderTest {

	@Test
	void widgetBuilderTest() {
		final WidgetRQ widgetRQ = new WidgetRQ();
		final String name = "name";
		widgetRQ.setName(name);
		final String description = "description";
		widgetRQ.setDescription(description);
		final boolean share = true;
		widgetRQ.setShare(share);
		final String widgetType = "oldLineChart";
		widgetRQ.setWidgetType(widgetType);
		final ContentParameters contentParameters = new ContentParameters();
		contentParameters.setContentFields(Collections.singletonList("contentField"));
		contentParameters.setItemsCount(10);
		final HashMap<String, Object> widgetOptions = new HashMap<>();
		widgetOptions.put("param", "val");
		contentParameters.setWidgetOptions(widgetOptions);
		widgetRQ.setContentParameters(contentParameters);
		final String owner = "owner";
		final UserFilter userFilter = new UserFilter();
		userFilter.setId(1L);

		final Widget widget = new WidgetBuilder().addWidgetRq(widgetRQ).addFilters(Collections.singletonList(userFilter)).addOwner(owner).get();

		assertEquals(name, widget.getName());
		assertEquals(description, widget.getDescription());
		assertEquals(share, widget.isShared());
		assertEquals(owner, widget.getOwner());
		assertEquals(widgetType, widget.getWidgetType());
		assertThat(widget.getFilters()).containsExactly(userFilter);
		assertThat(widget.getContentFields()).containsExactly("contentField");
		assertThat(widget.getWidgetOptions().getOptions()).containsExactly(new AbstractMap.SimpleEntry<>("param", "val"));
	}

	@Test
	void addWidgetPreviewRqTest() {
		final WidgetPreviewRQ previewRQ = new WidgetPreviewRQ();
		final ContentParameters contentParameters = new ContentParameters();
		contentParameters.setContentFields(Collections.singletonList("contentField"));
		contentParameters.setItemsCount(10);
		final HashMap<String, Object> widgetOptions = new HashMap<>();
		widgetOptions.put("param", "val");
		contentParameters.setWidgetOptions(widgetOptions);
		previewRQ.setContentParameters(contentParameters);
		final String widgetType = "oldLineChart";
		previewRQ.setWidgetType(widgetType);

		final Widget widget = new WidgetBuilder(new Widget()).addWidgetPreviewRq(previewRQ).get();

		assertEquals(widgetType, widget.getWidgetType());
		assertThat(widget.getContentFields()).containsExactly("contentField");
		assertThat(widget.getWidgetOptions().getOptions()).containsExactly(new AbstractMap.SimpleEntry<>("param", "val"));
	}
}