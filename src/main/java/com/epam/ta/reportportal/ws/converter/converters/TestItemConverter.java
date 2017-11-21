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

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.google.common.base.Preconditions;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

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
		resource.setDescription(item.getItemDescription());
		resource.setUniqueId(item.getUniqueId());
		resource.setTags(item.getTags());
		resource.setEndTime(item.getEndTime());
		resource.setItemId(item.getId());
		if (null != item.getParameters()) {
			resource.setParameters(item.getParameters().stream().map(ParametersConverter.TO_RESOURCE).collect(Collectors.toList()));
		}
		resource.setIssue(IssueConverter.TO_MODEL.apply(item.getIssue()));
		resource.setName(item.getName());
		resource.setStartTime(item.getStartTime());
		resource.setStatus(item.getStatus() != null ? item.getStatus().toString() : null);
		resource.setType(item.getType() != null ? item.getType().name() : null);
		resource.setParent(item.getParent());
		resource.setHasChilds(item.hasChilds());
		resource.setLaunchId(item.getLaunchRef());
		resource.setStatistics(StatisticsConverter.TO_RESOURCE.apply(item.getStatistics()));

		Optional.ofNullable(item.getRetries())
				.map(items -> items.stream().map(TestItemConverter.TO_RESOURCE)
						.sorted(comparing(TestItemResource::getStartTime))
						.collect(Collectors.toList()))
				.ifPresent(resource::setRetries);
		return resource;
	};
}
