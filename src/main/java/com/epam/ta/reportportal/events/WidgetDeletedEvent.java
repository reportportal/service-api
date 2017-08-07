package com.epam.ta.reportportal.events;

import com.epam.ta.reportportal.database.entity.widget.Widget;

public class WidgetDeletedEvent {

    private Widget widget;

    private String removerRef;

    public WidgetDeletedEvent(Widget widget, String removerId) {
        this.widget = widget;
        this.removerRef = removerId;
    }

    public Widget getWidget() {
        return widget;
    }

    public String getRemoverRef() {
        return removerRef;
    }
}
