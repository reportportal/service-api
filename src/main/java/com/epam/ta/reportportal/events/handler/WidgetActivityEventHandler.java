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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.events.WidgetUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

/**
 * @author Andrei Varabyeu
 */
@Component
public class WidgetActivityEventHandler {

	public static final String SHARE = "share";
	public static final String UNSHARE = "unshare";

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private Provider<ActivityBuilder> activityBuilder;

	@EventListener
	public void onWidgetUpdated(WidgetUpdatedEvent event) {
		Widget widget = event.getBefore();
		WidgetRQ widgetRQ = event.getWidgetRQ();
		if (null != widgetRQ.getShare()) {
			if (widget != null) {
				boolean isShared = !widget.getAcl().getEntries().isEmpty();
				if (!widgetRQ.getShare().equals(isShared)) {
					Activity activityLog = activityBuilder.get().addProjectRef(widget.getProjectName()).addObjectType(Widget.WIDGET)
							.addUserRef(event.getUpdatedBy()).addActionType(widgetRQ.getShare() ? SHARE : UNSHARE)
							.addLoggedObjectRef(widget.getId()).addObjectName(widgetRQ.getName()).build();
					activityRepository.save(activityLog);

				}
			}
		}
	}
}

