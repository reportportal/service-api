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
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemTag;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.store.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.store.commons.EntityUtils.update;
import static java.util.Optional.ofNullable;

public class TestItemBuilder implements Supplier<TestItem> {

	private TestItem testItem;

	public TestItemBuilder() {
		testItem = new TestItem();
	}

	public TestItemBuilder addStartItemRequest(StartTestItemRQ rq) {
		testItem.setStartTime(EntityUtils.TO_LOCAL_DATE_TIME.apply(rq.getStartTime()));
		testItem.setName(rq.getName().trim());
		testItem.setUniqueId(rq.getUniqueId());
		addDescription(rq.getDescription());
		addTags(rq.getTags());
		addParameters(rq.getParameters());
		addType(rq.getType());
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
		if (null != tags) {
			tags = Sets.newHashSet(trimStrings(update(tags)));
			testItem.setTags(tags.stream().map(TestItemTag::new).collect(Collectors.toSet()));
		}
		return this;
	}

	//TODO parameters
	public TestItemBuilder addParameters(List<ParameterResource> parameters) {
		//		if (null != parameters) {
		//			testItem.setParameters(parameters.stream().map(ParametersConverter.TO_MODEL).toArray(Parameter[]::new));
		//		}
		return this;
	}

	@Override
	public TestItem get() {
		return this.testItem;
	}
}