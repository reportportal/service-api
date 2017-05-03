/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.database.entity.project.KeepLogsDelay;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.actuate.info.MapInfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.epam.ta.reportportal.database.entity.project.KeepLogsDelay.values;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * @author Andrei Varabyeu
 */
@Component
public class MetadataInfoContributor extends MapInfoContributor {

	public MetadataInfoContributor() {
		super(ImmutableMap.<String, Object>builder().put("metadata", getKeepLogsDelays()).build());
	}

	private static Map<String, Object> getKeepLogsDelays() {
		return ImmutableMap.<String, Object>builder().put("keepLogsDelay", stream(values()).map(KeepLogsDelay::getValue).collect(toList()))
				.build();
	}
}
