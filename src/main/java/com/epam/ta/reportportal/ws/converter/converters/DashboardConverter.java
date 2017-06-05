package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DashboardConverter {

    private DashboardConverter() {
        //static only
    }

    public static final Function<Dashboard, DashboardResource> TO_RESOURCE = dashboard -> {
        DashboardResource resource = new DashboardResource();
        if (Optional.ofNullable(dashboard).isPresent()) {
            resource.setDashboardId(dashboard.getId());
            resource.setName(dashboard.getName());
            resource.setDescription(dashboard.getDescription());
            resource.setWidgets(Optional.ofNullable(dashboard.getWidgets())
                    .orElseGet(Collections::emptyList).stream()
                    .map(DashboardConverter.TO_WIDGET_RESOURCE)
                    .collect(Collectors.toList()));
            if (Optional.ofNullable(dashboard.getAcl()).isPresent()) {
                resource.setOwner(dashboard.getAcl().getOwnerUserId());
                resource.setShare(!dashboard.getAcl().getEntries().isEmpty());
            }
        }
        return resource;
    };

    private static final Function<Dashboard.WidgetObject, DashboardResource.WidgetObjectModel> TO_WIDGET_RESOURCE = widgetObject -> {
        DashboardResource.WidgetObjectModel resource = new DashboardResource.WidgetObjectModel();
        resource.setWidgetId(widgetObject.getWidgetId());
        resource.setWidgetPosition(widgetObject.getWidgetPosition());
        resource.setWidgetSize(widgetObject.getWidgetSize());
        return resource;
    };

}
