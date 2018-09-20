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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemTag;
import com.epam.ta.reportportal.ws.model.TestItemResource;

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
		resource.setItemId(String.valueOf(item.getItemId()));
		if (null != item.getParameters()) {
			resource.setParameters(item
					.getParameters()
					.stream()
					.map(ParametersConverter.TO_RESOURCE)
					.collect(Collectors.toList()));
		}
		Optional.ofNullable(item.getItemResults().getIssue()).ifPresent(i -> resource.setIssue(IssueConverter.TO_MODEL.apply(i)));
		resource.setName(item.getName());
		resource.setStartTime(EntityUtils.TO_DATE.apply(item.getStartTime()));
		resource.setStatus(item.getItemResults().getStatus() != null ? item.getItemResults().getStatus().toString() : null);
		resource.setType(item.getType() != null ? item.getType().name() : null);

		//FIXME: provide correct parameters
		resource.setHasChilds(false);

		if (item.getParent() != null) {
			resource.setParent(item.getParent().getItemId());
		}
		resource.setLaunchId(item.getLaunch().getId());
		resource.setStatisticsResource(StatisticsConverter.TO_RESOURCE.apply(item.getItemResults().getStatistics()));
		return resource;
	};
}
