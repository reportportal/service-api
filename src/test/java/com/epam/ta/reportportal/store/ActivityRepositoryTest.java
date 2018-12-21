package com.epam.ta.reportportal.store;

import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@ImportDataset("classpath:db/dataset.xml")
@Ignore
public class ActivityRepositoryTest extends BaseDBTest {

	@Autowired
	private ActivityRepository activityRepository;

	@Test
	public void shouldDeleteUser() {
		Activity activity = new Activity();
		activity.setAction("some action");
		activity.setCreatedAt(LocalDateTime.now());
		activity.setObjectId(100L);
		activity.setDetails(new ActivityDetails("some name"));
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM);
		activity.setUserId(1L);

		activity = activityRepository.save(activity);

		Assertions.assertThat(activity.getId()).isNotNull();
	}
}
