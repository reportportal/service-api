/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.*;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;

public class TestItemBuilder implements Supplier<TestItem> {

	private TestItem testItem;

	private TestItemStructure structure;

	public TestItemBuilder() {
		testItem = new TestItem();
		structure = new TestItemStructure();
	}

	public TestItemBuilder(TestItem testItem) {
		this.testItem = testItem;
		this.structure = testItem.getItemStructure();
	}

	public TestItemBuilder addStartItemRequest(StartTestItemRQ rq) {

		testItem.setStartTime(EntityUtils.TO_LOCAL_DATE_TIME.apply(rq.getStartTime()));
		testItem.setName(rq.getName().trim());
		testItem.setUniqueId(rq.getUniqueId());

		TestItemResults testItemResults = new TestItemResults();
		testItemResults.setStatus(StatusEnum.IN_PROGRESS);

		structure.setItemResults(testItemResults);
		testItemResults.setItemStructure(structure);

		addDescription(rq.getDescription());
		addTags(rq.getTags());
		addParameters(rq.getParameters());
		addType(rq.getType());
		return this;
	}

	public TestItemBuilder addLaunch(Launch launch) {
		structure.setLaunch(launch);
		return this;
	}

	public TestItemBuilder addParent(TestItemStructure parentStructure) {
		structure.setParent(parentStructure);
		return this;
	}

	public TestItemBuilder addType(String typeValue) {
		TestItemTypeEnum type = TestItemTypeEnum.fromValue(typeValue);
		BusinessRule.expect(type, Objects::nonNull).verify(ErrorType.UNSUPPORTED_TEST_ITEM_TYPE, typeValue);
		testItem.setType(type);
		return this;
	}

	public TestItemBuilder addDescription(String description) {
		ofNullable(description).ifPresent(it -> testItem.setDescription(it.trim()));
		return this;
	}

	public TestItemBuilder addTags(Set<String> tags) {
		ofNullable(tags).ifPresent(it -> testItem.setTags(it.stream()
				.filter(EntityUtils.NOT_EMPTY)
				.map(EntityUtils.REPLACE_SEPARATOR)
				.map(TestItemTag::new)
				.collect(Collectors.toSet())));
		return this;
	}

	public TestItemBuilder addTestItemResults(TestItemResults testItemResults) {
		checkNotNull(testItemResults, "Provided value shouldn't be null");
		structure.setItemResults(testItemResults);
		addDuration(testItemResults.getEndTime());
		return this;
	}

	public TestItemBuilder addDuration(LocalDateTime endTime) {
		checkNotNull(endTime, "Provided value shouldn't be null");
		checkNotNull(structure.getItemResults(), "Test item results shouldn't be null");

		//converts to seconds
		structure.getItemResults().setDuration(ChronoUnit.MILLIS.between(testItem.getStartTime(), endTime) / 1000d);
		return this;
	}

	public TestItemBuilder addParameters(List<ParameterResource> parameters) {
		if (!CollectionUtils.isEmpty(parameters)) {
			testItem.setParameters(parameters.stream().map(it -> {
				Parameter parameter = new Parameter();
				parameter.setKey(it.getKey());
				parameter.setValue(it.getValue());
				return parameter;
			}).collect(Collectors.toList()));
		}
		return this;
	}

	@Override
	public TestItem get() {
		structure.setTestItem(testItem);
		testItem.setItemStructure(structure);
		return this.testItem;
	}

}