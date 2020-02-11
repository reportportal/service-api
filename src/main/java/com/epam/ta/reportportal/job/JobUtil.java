/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;

import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ATTRIBUTE_NAME;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class JobUtil {

	private JobUtil() {
		// static only
	}

	static Filter buildProjectAttributesFilter(ProjectAttributeEnum projectAttributeEnum) {
		return Filter.builder()
				.withTarget(Project.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ATTRIBUTE_NAME, projectAttributeEnum.getAttribute()).build())
				.build();
	}
}
