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

package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_STATUS;

/**
 * @author Pavel Bortnik
 */
@Service("generalLaunchFilterStrategy")
public class GeneralLaunchFilterStrategy extends ProjectFilterStrategy {

	@Override
	protected Filter buildDefaultFilter(Widget widget, Long projectId) {
		Filter filter = super.buildDefaultFilter(widget, projectId);
		filter.withConditions(Sets.newHashSet(
				new FilterCondition(Condition.NOT_EQUALS, false, StatusEnum.IN_PROGRESS.name(), CRITERIA_LAUNCH_STATUS),
				new FilterCondition(Condition.EQUALS, false, Mode.DEFAULT.toString(), CRITERIA_LAUNCH_MODE)
		));
		return filter;
	}
}
