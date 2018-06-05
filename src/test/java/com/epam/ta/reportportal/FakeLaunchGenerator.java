/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.store.database.entity.launch.Launch;

/**
 * @author Dzianis_Shybeka
 */
public class FakeLaunchGenerator implements FakeDataGenerator<Launch> {

	static final Long TEST_LAUNCH_ID = 1233L;

	private Long launchId = TEST_LAUNCH_ID;

	private final Launch launch;

	public FakeLaunchGenerator() {

		launch = new Launch();
	}

	public Launch generate() {

		launch.setId(launchId);

		return launch;
	}
}
