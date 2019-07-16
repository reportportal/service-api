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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.User;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class LaunchTestUtil {

	private LaunchTestUtil() {
		//static only
	}

	public static Optional<Launch> getLaunch(StatusEnum status, LaunchModeEnum mode) {
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setNumber(1L);
		launch.setProjectId(1L);
		launch.setStatus(status);
		launch.setStartTime(LocalDateTime.now().minusHours(3));
		User user = new User();
		user.setLogin("test");
		launch.setUser(user);
		launch.setMode(mode);
		launch.setUuid("uuid");
		launch.setDescription("description");
		launch.setName("launch name");
		return Optional.of(launch);
	}
}
