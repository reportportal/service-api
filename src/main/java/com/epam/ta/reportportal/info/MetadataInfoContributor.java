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
package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.enums.ActivityEventType;
import com.epam.ta.reportportal.entity.enums.KeepLogsDelay;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.actuate.info.MapInfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * @author Andrei Varabyeu
 */
@Component
public class MetadataInfoContributor extends MapInfoContributor {

	public MetadataInfoContributor() {
		super(ImmutableMap.<String, Object>builder().put("metadata", getMetadata()).build());
	}

	private static Map<String, Object> getMetadata() {
		return ImmutableMap.<String, Object>builder().put(
				"activitiesEventType", stream(ActivityEventType.values()).map(ActivityEventType::getValue).collect(toList()))
				.put(
						"activitiesObjectType",
						stream(Activity.ActivityEntityType.values()).map(Activity.ActivityEntityType::getValue).collect(toList())
				)
				.put("keepLogsDelay", stream(KeepLogsDelay.values()).map(KeepLogsDelay::getValue).collect(toList()))
				.build();
	}
}
