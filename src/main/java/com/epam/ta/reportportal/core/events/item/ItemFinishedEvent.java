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

package com.epam.ta.reportportal.core.events.item;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ItemFinishedEvent {

	private final Long itemId;

	private final Long launchId;

	private final Long projectId;

	public ItemFinishedEvent(Long itemId, Long launchId, Long projectId) {
		this.itemId = itemId;
		this.launchId = launchId;
		this.projectId = projectId;
	}

	public Long getItemId() {
		return itemId;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public Long getProjectId() {
		return projectId;
	}
}
