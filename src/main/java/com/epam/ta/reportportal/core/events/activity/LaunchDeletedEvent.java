/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.entity.launch.Launch;

/**
 * @author Andrei Varabyeu
 */
public class LaunchDeletedEvent {
	private final Launch launch;
	private final String deletedBy;

	public LaunchDeletedEvent(Launch launch, String deletedBy) {
		this.launch = launch;
		this.deletedBy = deletedBy;
	}

	public Launch getLaunch() {
		return launch;
	}

	public String getDeletedBy() {
		return deletedBy;
	}
}
