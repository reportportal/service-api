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
import com.epam.ta.reportportal.database.entity.item.ActivityEventType;
import com.epam.ta.reportportal.database.entity.item.ActivityObjectType;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.google.common.collect.ImmutableList;
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
		activity.setActionType(ActivityEventType.START_LAUNCH);
		activity.setLastModifiedDate(new Date(0));
		activity.setHistory(ImmutableList.<Activity.FieldValues>builder().add(
				new Activity.FieldValues().withField(KEY).withOldValue("old").withNewValue("new")).build());
		activity.setLoggedObjectRef("objectRef");
		activity.setObjectType(ActivityObjectType.LAUNCH);
		activity.setProjectRef("project");
		activity.setUserRef("user");
		validate(activity, ActivityConverter.TO_RESOURCE.apply(activity));
	}

	private void validate(Activity db, ActivityResource resource) {
		Assert.assertEquals(db.getLastModified(), resource.getLastModifiedDate());
		Assert.assertEquals(db.getId(), resource.getActivityId());
		Assert.assertEquals(db.getActionType(), ActivityEventType.fromString(resource.getActionType()).get());
		Assert.assertEquals(db.getLoggedObjectRef(), resource.getLoggedObjectRef());
		Assert.assertEquals(db.getObjectType(), ActivityObjectType.fromString(resource.getObjectType()).get());
		Assert.assertEquals(db.getProjectRef(), resource.getProjectRef());
		Assert.assertEquals(db.getUserRef(), resource.getUserRef());
		Activity.FieldValues expected = db.getHistory().get(0);
		ActivityResource.FieldValues actual = resource.getHistory().get(0);
		Assert.assertEquals(expected.getField(), actual.getField());
		Assert.assertEquals(expected.getNewValue(), actual.getNewValue());
		Assert.assertEquals(expected.getOldValue(), actual.getOldValue());
	}

}