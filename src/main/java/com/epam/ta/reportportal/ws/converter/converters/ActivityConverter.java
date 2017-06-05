package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ActivityConverter {

    private ActivityConverter() {
        //static only
    }

    public final static Function<Activity, ActivityResource> TO_RESOURCE = activity -> {
        ActivityResource resource = new ActivityResource();
        if (Optional.ofNullable(activity).isPresent()) {
            resource.setUserRef(activity.getUserRef());
            resource.setActivityId(activity.getId());
            resource.setLoggedObjectRef(activity.getLoggedObjectRef());
            resource.setLastModifiedDate(activity.getLastModified());
            resource.setObjectType(activity.getObjectType());
            resource.setActionType(activity.getActionType());
            Map<String, ActivityResource.FieldValues> history =
                    Optional.ofNullable(activity.getHistory())
                            .orElseGet(Collections::emptyMap)
                            .entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                    e -> ActivityConverter.TO_FIELD_RESOURCE.apply(e.getValue())));
            resource.setHistory(history);
        }
        return resource;

    };

    private static final Function<Activity.FieldValues, ActivityResource.FieldValues> TO_FIELD_RESOURCE = model -> {
        ActivityResource.FieldValues fieldValues = new ActivityResource.FieldValues();
        fieldValues.setOldValue(model.getOldValue());
        fieldValues.setNewValue(model.getNewValue());
        return fieldValues;
    };

}
