package com.epam.ta.reportportal.store;

import com.epam.ta.reportportal.store.database.dao.ActivityRepository;
import com.epam.ta.reportportal.store.database.entity.Activity;
import com.epam.ta.reportportal.store.database.entity.JsonMap;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.time.LocalDateTime;

@ImportDataset("classpath:db/dataset.xml")
public class ActivityRepositoryTest extends BaseDBTest {

	@Autowired
	private ActivityRepository activityRepository;

	@Test
	public void shouldDeleteUser() {
		Activity activity = new Activity();
		activity.setAction("some action");
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(JsonMap.ofMap(ImmutableMap.<String, String>builder().put("key", "value").build()));
		activity.setEntity(Activity.Entity.ITEM);
		activity.setUserId(1L);

		activity = activityRepository.save(activity);

		Assertions.assertThat(activity.getId()).isNotNull();
	}
}
