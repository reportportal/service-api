/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemTag;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;

import java.util.Optional;
import java.util.function.Function;
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
		TestItemResource resource = new TestItemResource();
		resource.setDescription(item.getDescription());
		resource.setUniqueId(item.getUniqueId());
		resource.setTags(item.getTags().stream().map(TestItemTag::getValue).collect(Collectors.toSet()));
		resource.setEndTime(EntityUtils.TO_DATE.apply(item.getItemResults().getEndTime()));
		resource.setItemId(item.getItemId());
		if (null != item.getParameters()) {
			resource.setParameters(item.getParameters().stream().map(ParametersConverter.TO_RESOURCE).collect(Collectors.toList()));
		}
		Optional.ofNullable(item.getItemResults().getIssue()).ifPresent(i -> resource.setIssue(IssueConverter.TO_MODEL.apply(i)));
		resource.setName(item.getName());
		resource.setStartTime(EntityUtils.TO_DATE.apply(item.getStartTime()));
		resource.setStatus(item.getItemResults().getStatus() != null ? item.getItemResults().getStatus().toString() : null);
		resource.setType(item.getType() != null ? item.getType().name() : null);
		resource.setHasChildren(item.isHasChildren());

		resource.setHasChildren(item.isHasChildren());

		if (item.getParent() != null) {
			resource.setParent(item.getParent().getItemId());
		}
		resource.setLaunchId(item.getLaunch().getId());
		resource.setPath(item.getPath());
		resource.setStatisticsResource(StatisticsConverter.TO_RESOURCE.apply(item.getItemResults().getStatistics()));
		return resource;
	};

	public static final Function<TestItem, TestItemActivityResource> TO_ACTIVITY_RESOURCE = testItem -> {
		TestItemActivityResource resource = new TestItemActivityResource();
		resource.setId(testItem.getLaunch().getProjectId());
		resource.setName(testItem.getName());
		resource.setAutoAnalyzed(testItem.getItemResults().getIssue().getAutoAnalyzed());
		resource.setIgnoreAnalyzer(testItem.getItemResults().getIssue().getIgnoreAnalyzer());
		resource.setIssueDescription(testItem.getItemResults().getIssue().getIssueDescription());
		resource.setIssueTypeLongName(testItem.getItemResults().getIssue().getIssueType().getLongName());
		resource.setTickets(testItem.getItemResults()
				.getIssue()
				.getTickets()
				.stream()
				.map(it -> it.getTicketId().concat(":").concat(it.getUrl()))
				.collect(Collectors.joining(",")));
		return resource;
	};
}
