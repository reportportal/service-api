package com.epam.ta.reportportal.events;

import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;

public class WidgetCreatedEvent {

    private WidgetRQ widgetRQ;

    private String creatorRef;

    private String projectRef;

    private String widgetId;

    public WidgetCreatedEvent(WidgetRQ widgetRQ, String creatorRef, String projectRef, String widgetId) {
        this.widgetRQ = widgetRQ;
        this.creatorRef = creatorRef;
        this.projectRef = projectRef;
        this.widgetId = widgetId;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public WidgetRQ getWidgetRQ() {
        return widgetRQ;
    }

    public String getCreatorRef() {
        return creatorRef;
    }

    public String getProjectRef() {
        return projectRef;
    }
}
