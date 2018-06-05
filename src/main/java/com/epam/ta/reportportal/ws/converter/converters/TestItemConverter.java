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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemTag;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class TestItemConverter {

	private TestItemConverter() {
		//static only
	}

	public static final Function<TestItem, TestItemResource> TO_RESOURCE = item -> {

		Preconditions.checkNotNull(item);

		TestItemResource resource = new TestItemResource();
		resource.setItemId(String.valueOf(item.getItemId()));
		resource.setDescription(item.getDescription());
		resource.setUniqueId(item.getUniqueId());
		resource.setTags(getTags(item));
		resource.setEndTime(getEndTime(item));
		if (null != item.getParameters()) {
			resource.setParameters(item.getParameters().stream().map(ParametersConverter.TO_RESOURCE).collect(Collectors.toList()));
		}
		resource.setIssue(getIssue(item));
		resource.setName(item.getName());
		resource.setStartTime(EntityUtils.TO_DATE.apply(item.getStartTime()));
		resource.setStatus(getStatus(item));
		resource.setType(item.getType() != null ? item.getType().name() : null);

		resource.setParent(getParent(item));
		resource.setLaunchId(getLaunchId(item));

		return resource;
	};

	private static String getParent(TestItem item) {

		return Optional.ofNullable(item.getTestItemStructure())
				.map(testItemStructure -> testItemStructure.getParent() == null ?
						"" :
						Objects.toString(testItemStructure.getParent().getItemId(), StringUtils.EMPTY))
				.orElse(StringUtils.EMPTY);
	}

	private static Issue getIssue(TestItem item) {

		return Optional.ofNullable(item.getTestItemResults())
				.map(results -> IssueConverter.TO_MODEL.apply(results.getIssue()))
				.orElse(null);
	}

	private static String getLaunchId(TestItem item) {
		return Optional.ofNullable(item.getLaunch()).map(launch -> launch.getId().toString()).orElse(null);
	}

	private static String getStatus(TestItem item) {

		return Optional.ofNullable(item.getTestItemResults()).map(results -> results.getStatus().toString()).orElse(null);
	}

	private static Date getEndTime(TestItem item) {
		return Optional.ofNullable(item.getTestItemResults())
				.flatMap(results -> Optional.ofNullable(results.getEndTime()))
				.map(EntityUtils.TO_DATE)
				.orElse(null);
	}

	private static Set<String> getTags(TestItem item) {
		return Optional.ofNullable(item.getTags())
				.map(tags -> tags.stream().map(TestItemTag::getValue).collect(Collectors.toSet()))
				.orElse(Sets.newHashSet());
	}
}
