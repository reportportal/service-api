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

    public static final Function<Dashboard, DashboardResource> TO_RESOURCE = model -> {
        DashboardResource resource = new DashboardResource();
        if (Optional.ofNullable(model).isPresent()) {
            resource.setDashboardId(model.getId());
            resource.setName(model.getName());
            resource.setDescription(model.getDescription());
            resource.setWidgets(Optional.ofNullable(model.getWidgets())
                    .orElseGet(Collections::emptyList).stream()
                    .map(DashboardConverter.TO_WIDGET_RESOURCE)
                    .collect(Collectors.toList()));

            if (Optional.ofNullable(model.getAcl()).isPresent()) {
                resource.setOwner(model.getAcl().getOwnerUserId());
                resource.setIsShared(!model.getAcl().getEntries().isEmpty());
            }
        }
        return resource;
    };

    private static final Function<Dashboard.WidgetObject, DashboardResource.WidgetObjectModel> TO_WIDGET_RESOURCE = model -> {
        DashboardResource.WidgetObjectModel resource = new DashboardResource.WidgetObjectModel();
        resource.setWidgetId(model.getWidgetId());
        resource.setWidgetPosition(model.getWidgetPosition());
        resource.setWidgetSize(model.getWidgetSize());
        return resource;
    };

}
