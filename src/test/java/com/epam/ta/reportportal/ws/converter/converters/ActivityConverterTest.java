package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class ActivityConverterTest {

    private static final String KEY = "key";

    @Test(expected = NullPointerException.class)
    public void testNull() {
        ActivityConverter.TO_RESOURCE.apply(null);
    }

    @Test
    public void testConvert() {
        Activity activity = new Activity();
        activity.setId("id");
        activity.setActionType("action");
        activity.setLastModifiedDate(new Date(0));
        activity.setHistory(ImmutableMap.<String, Activity.FieldValues>builder()
                .put(KEY , new Activity.FieldValues().withOldValue("old").withNewValue("new")).build());
        activity.setLoggedObjectRef("objectRef");
        activity.setObjectType("objectType");
        activity.setProjectRef("project");
        activity.setUserRef("user");
        validate(activity, ActivityConverter.TO_RESOURCE.apply(activity));
    }

    private void validate(Activity db, ActivityResource resource) {
        Assert.assertEquals(db.getLastModified(), resource.getLastModifiedDate());
        Assert.assertEquals(db.getId(), resource.getActivityId());
        Assert.assertEquals(db.getActionType(), resource.getActionType());
        Assert.assertEquals(db.getLoggedObjectRef(), resource.getLoggedObjectRef());
        Assert.assertEquals(db.getObjectType(), resource.getObjectType());
        Assert.assertEquals(db.getProjectRef(), resource.getProjectRef());
        Assert.assertEquals(db.getUserRef(), resource.getUserRef());
        Assert.assertEquals(db.getHistory().get(KEY).getNewValue(),
                resource.getHistory().get(KEY).getNewValue());
        Assert.assertEquals(db.getHistory().get(KEY).getOldValue(),
                resource.getHistory().get(KEY).getOldValue());
    }

}