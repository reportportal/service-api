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
import com.epam.ta.reportportal.store.database.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemTag;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

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
		//testItem.setStartTime(new Timestamp(rq.getStartTime().getTime()));
		testItem.setName(rq.getName().trim());
		testItem.setUniqueId(rq.getUniqueId());
		ofNullable(rq.getDescription()).ifPresent(it -> testItem.setDescription(it.trim()));

		Set<String> tags = rq.getTags();
		if (!CollectionUtils.isEmpty(tags)) {
			tags = Sets.newHashSet(trimStrings(update(tags)));
			testItem.setTags(tags.stream().map(TestItemTag::new).collect(Collectors.toSet()));
		}
		List<ParameterResource> parameters = rq.getParameters();
		//		if (null != parameters) {
		//			testItem.setParameters(parameters.stream().map(ParametersConverter.TO_MODEL).toArray(Parameter[]::new));
		//		}
		TestItemTypeEnum type = TestItemTypeEnum.fromValue(rq.getType());
		BusinessRule.expect(type, Objects::nonNull).verify(ErrorType.UNSUPPORTED_TEST_ITEM_TYPE, rq.getType());
		testItem.setType(type);
		return this;
	}

	@Override
	public TestItem get() {
		return this.testItem;
	}
}