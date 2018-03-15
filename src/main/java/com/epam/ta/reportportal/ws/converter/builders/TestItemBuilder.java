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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.ws.converter.converters.ParametersConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.google.common.collect.Sets;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
@Scope("prototype")
public class TestItemBuilder extends Builder<TestItem> {

	public TestItemBuilder addStartItemRequest(StartTestItemRQ rq) {
		getObject().setStartTime(Optional.ofNullable(rq.getStartTime()).orElse(Date.from(Instant.now())));
		getObject().setName(rq.getName().trim());
		getObject().setUniqueId(rq.getUniqueId());
		if (null != rq.getDescription()) {
			getObject().setItemDescription(rq.getDescription().trim());
		}
		Set<String> tags = rq.getTags();
		if (null != tags) {
			tags = Sets.newHashSet(EntityUtils.trimStrings(EntityUtils.update(tags)));
		}
		List<ParameterResource> parameters = rq.getParameters();
		if (null != parameters) {
			getObject().setParameters(parameters.stream().map(ParametersConverter.TO_MODEL).collect(toList()));
		}
		getObject().setTags(tags);
		TestItemType type = TestItemType.fromValue(rq.getType());
		BusinessRule.expect(type, Predicates.notNull()).verify(ErrorType.UNSUPPORTED_TEST_ITEM_TYPE, rq.getType());
		getObject().setType(type);
		return this;
	}

	public TestItemBuilder addStatus(Status status) {
		getObject().setStatus(status);
		return this;
	}

	public TestItemBuilder addParent(TestItem parent) {
		getObject().setLaunchRef(parent.getLaunchRef());
		getObject().setParent(parent.getId());
		return this;
	}

	public TestItemBuilder addPath(TestItem parent) {
		if (parent.getPath() != null && !parent.getPath().isEmpty()) {
			getObject().getPath().addAll(parent.getPath());
		}
		getObject().getPath().add(parent.getId());
		return this;
	}

	public TestItemBuilder addLaunch(Launch launch) {
		getObject().setLaunchRef(launch.getId());
		return this;
	}

	@Override
	protected TestItem initObject() {
		return new TestItem();
	}
}