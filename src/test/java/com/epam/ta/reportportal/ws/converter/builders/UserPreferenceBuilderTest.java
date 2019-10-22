/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.preference.UserPreference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class UserPreferenceBuilderTest {

	@Test
	void userPreferenceBuilder() {
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