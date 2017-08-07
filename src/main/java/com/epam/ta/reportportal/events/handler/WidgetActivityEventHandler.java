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
import com.epam.ta.reportportal.events.WidgetCreatedEvent;
import com.epam.ta.reportportal.events.WidgetDeletedEvent;
import com.epam.ta.reportportal.events.WidgetUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.HashMap;

import static com.epam.ta.reportportal.core.widget.impl.WidgetUtils.NAME;

/**
 * @author Andrei Varabyeu
 */
@Component
public class WidgetActivityEventHandler {

	public static final String SHARE = "share";
	public static final String UNSHARE = "unshare";
    private static final String UPDATE_WIDGET = "update_widget";
    private static final String DESCRIPTION = "description";
    private static final String CREATE_WIDGET = "create_widget";
    private static final String DELETE_WIDGET = "delete_widget";

    //for created or deleted widgets
    private static final String EMPTY_FIELD = "";

    @Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private Provider<ActivityBuilder> activityBuilder;

	@EventListener
	public void onWidgetUpdated(WidgetUpdatedEvent event) {
		Widget widget = event.getBefore();
		WidgetRQ widgetRQ = event.getWidgetRQ();
        HashMap<String, Activity.FieldValues> history = new HashMap<>();
		if (widget != null) {
		    processShare(history, widget, widgetRQ.getShare());
            processWidgetName(history, widget, widgetRQ.getName());
            processDescription(history, widget, widgetRQ.getDescription());
            if (!history.isEmpty()) {
                Activity activityLog = activityBuilder.get()
                        .addProjectRef(widget.getProjectName())
                        .addObjectType(Widget.WIDGET)
                        .addActionType(UPDATE_WIDGET)
                        .addLoggedObjectRef(widget.getId())
                        .addUserRef(event.getUpdatedBy())
                        .build();
                activityLog.setHistory(history);
                activityRepository.save(activityLog);
            }
        }
	}

	@EventListener
    public void onCreateWidget(WidgetCreatedEvent event) {
        WidgetRQ widgetRQ = event.getWidgetRQ();
        Activity activityLog = activityBuilder.get()
                .addActionType(CREATE_WIDGET)
                .addObjectType(Widget.WIDGET)
                .addObjectName(widgetRQ.getName())
                .addProjectRef(event.getProjectRef())
                .addUserRef(event.getCreatorRef())
                .addLoggedObjectRef(event.getWidgetId())
                .addHistory(ImmutableMap.<String, Activity.FieldValues>builder()
                        .put(NAME, createHistoryField("", widgetRQ.getName())).build())
                .build();
        activityRepository.save(activityLog);

    }

    @EventListener
    public void onDeleteWidget(WidgetDeletedEvent event) {
        Widget widget = event.getWidget();
        Activity activityLog = this.activityBuilder.get()
                .addActionType(DELETE_WIDGET)
                .addObjectType(Widget.WIDGET)
                .addObjectName(widget.getName())
                .addProjectRef(widget.getProjectName())
                .addUserRef(event.getRemoverRef())
                .addHistory(ImmutableMap.<String, Activity.FieldValues>builder()
                        .put(NAME, createHistoryField(widget.getName(), "")).build())
                .build();
        activityRepository.save(activityLog);
    }

    private void processDescription(HashMap<String, Activity.FieldValues> history, Widget widget, String newDescription) {
        if (null != newDescription && !widget.getDescription().equals(newDescription)) {
            history.put(DESCRIPTION, createHistoryField(widget.getDescription(), newDescription));
        }
    }

    private void processShare(HashMap<String, Activity.FieldValues> history, Widget widget, Boolean share) {
        if (null != share) {
            Boolean isShared = !widget.getAcl().getEntries().isEmpty();
            if (!share.equals(isShared)) {
                history.put(SHARE, createHistoryField(isShared.toString(), share.toString()));
            }
        }
    }

    private void processWidgetName(HashMap<String, Activity.FieldValues> history, Widget widget, String newWidgetName) {
        if (!Strings.isNullOrEmpty(newWidgetName) && !widget.getName().equals(newWidgetName)) {
            history.put(NAME, createHistoryField(widget.getName(), newWidgetName));
        }
    }

    private Activity.FieldValues createHistoryField(String oldValue, String newValue) {
	    return Activity.FieldValues.newOne().withOldValue(oldValue).withNewValue(newValue);
    }
}

