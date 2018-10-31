/*
 * Copyright (C) 2018 EPAM Systems
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
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;

import java.util.function.Supplier;

/**
 * @author Pavel Bortnik
 */
public class UserPreferenceBuilder implements Supplier<UserPreference> {

	private UserPreference userPreference;

	public UserPreferenceBuilder() {
		userPreference = new UserPreference();
	}

	public UserPreferenceBuilder withProject(Long id) {
		Project project = new Project();
		project.setId(id);
		userPreference.setProject(project);
		return this;
	}

	public UserPreferenceBuilder withUser(Long id) {
		User user = new User();
		user.setId(id);
		userPreference.setUser(user);
		return this;
	}

	public UserPreferenceBuilder withFilter(UserFilter filter) {
		userPreference.setFilter(filter);
		return this;
	}

	@Override
	public UserPreference get() {
		return userPreference;
	}
}
