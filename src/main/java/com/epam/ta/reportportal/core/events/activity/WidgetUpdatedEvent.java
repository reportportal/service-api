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
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;

/**
 * @author Andrei Varabyeu
 */
public class WidgetUpdatedEvent extends BeforeEvent<Widget> {

	private final WidgetRQ widgetRQ;
	private final String updatedBy;

	public WidgetUpdatedEvent(Widget before, WidgetRQ widgetRQ, String updatedBy) {
		super(before);
		this.widgetRQ = widgetRQ;
		this.updatedBy = updatedBy;
	}

	public WidgetRQ getWidgetRQ() {
		return widgetRQ;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}
}
