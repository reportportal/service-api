package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.preference.UserPreference;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class UserPreferenceBuilderTest {

	@Test
	public void userPreferenceBuilder() {
		final UserFilter filter = new UserFilter();
		filter.setName("name");
		filter.setOwner("owner");
		final Long projectId = 1L;
		final Long userId = 2L;

		final UserPreference userPreference = new UserPreferenceBuilder().withFilter(filter).withProject(projectId).withUser(userId).get();

		assertThat(userPreference.getFilter()).isEqualToComparingFieldByField(filter);
		assertEquals(projectId, userPreference.getProject().getId());
		assertEquals(userId, userPreference.getUser().getId());
	}
}