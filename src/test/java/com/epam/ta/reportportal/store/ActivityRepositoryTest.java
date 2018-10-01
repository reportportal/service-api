package com.epam.ta.reportportal.store;

import com.epam.ta.reportportal.core.events.activity.details.SimpleUserActivityDetails;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.entity.Activity;
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
		activity.setDetails(new SimpleUserActivityDetails(100L));
		activity.setEntity(Activity.Entity.ITEM);
		activity.setUserId(1L);

		activity = activityRepository.save(activity);

		Assertions.assertThat(activity.getId()).isNotNull();
	}
}
