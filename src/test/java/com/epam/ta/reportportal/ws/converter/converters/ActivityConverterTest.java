/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * @author Pavel_Bortnik
 */
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