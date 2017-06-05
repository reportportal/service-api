package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;

import java.util.Optional;
import java.util.function.Function;

public final class WidgetConverter {

    private WidgetConverter() {
        //static only
    }

    public static final Function<Widget, WidgetResource> TO_RESOURCE = widget -> {
        WidgetResource widgetResource = new WidgetResource();
        if (Optional.ofNullable(widget).isPresent()) {
            widgetResource.setWidgetId(widget.getId());
            widgetResource.setName(widget.getName());
            widgetResource.setDescription(widget.getDescription());
            widgetResource.setApplyingFilterID(Optional.ofNullable(widget.getApplyingFilterId())
                    .orElse(null));
            if (Optional.ofNullable(widget.getContentOptions()).isPresent()) {
                ContentParameters contentParameters = new ContentParameters();
                contentParameters.setType(widget.getContentOptions().getType());
                contentParameters.setGadget(widget.getContentOptions().getGadgetType());
                contentParameters.setMetadataFields(widget.getContentOptions().getMetadataFields());
                contentParameters.setContentFields(widget.getContentOptions().getContentFields());
                contentParameters.setItemsCount(widget.getContentOptions().getItemsCount());
                contentParameters.setWidgetOptions(widget.getContentOptions().getWidgetOptions());
                widgetResource.setContentParameters(contentParameters);
            }
            if (Optional.ofNullable(widget.getAcl()).isPresent()) {
                widgetResource.setOwner(widget.getAcl().getOwnerUserId());
                widgetResource.setShare(!widget.getAcl().getEntries().isEmpty());
            }
        }
        return widgetResource;
    };
}
