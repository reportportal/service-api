package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ActivityConverterTest {

	@Test(expected = NullPointerException.class)
	public void testNull() {
		ActivityConverter.TO_RESOURCE.apply(null);
	}

	@Test
	public void testConvert() {
		Activity activity = new Activity();
		activity.setId(1L);
		activity.setAction("LAUNCH_STARTED");
		activity.setCreatedAt(LocalDateTime.now());
		final ActivityDetails details = new ActivityDetails();
		details.setObjectName("objectName");
		details.setHistory(Collections.singletonList(HistoryField.of("filed", "old", "new")));
		activity.setDetails(details);
		activity.setUsername("username");
		activity.setActivityEntityType(Activity.ActivityEntityType.LAUNCH);
		activity.setProjectId(2L);
		activity.setUserId(3L);
		validate(activity, ActivityConverter.TO_RESOURCE.apply(activity));
	}

	@Test
	public void toResourceWithUser() {
		Activity activity = new Activity();
		activity.setId(1L);
		activity.setAction("LAUNCH_STARTED");
		activity.setCreatedAt(LocalDateTime.now());
		final ActivityDetails details = new ActivityDetails();
		details.setObjectName("objectName");
		details.setHistory(Collections.singletonList(HistoryField.of("filed", "old", "new")));
		activity.setDetails(details);
		activity.setActivityEntityType(Activity.ActivityEntityType.LAUNCH);
		activity.setProjectId(2L);
		activity.setUserId(3L);
		final ActivityResource resource = ActivityConverter.TO_RESOURCE_WITH_USER.apply(activity, "username");
		assertEquals("username", resource.getUser());
	}

	private void validate(Activity db, ActivityResource resource) {
		assertEquals(Date.from(db.getCreatedAt().atZone(ZoneId.of("UTC")).toInstant()), resource.getLastModified());
		assertEquals(db.getId(), resource.getId());
		assertEquals(db.getActivityEntityType(), Activity.ActivityEntityType.fromString(resource.getObjectType()).get());
		assertEquals(String.valueOf(db.getUserId()), resource.getUser());
		assertEquals(db.getProjectId(), resource.getProjectId());
		assertEquals(db.getAction(), resource.getActionType());
	}
}