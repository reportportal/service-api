/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.pattern;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.launch.Launch;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PatternAnalyzer {

	/**
	 * Analyze by {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate#value}
	 * all {@link com.epam.ta.reportportal.entity.log.Log#logMessage} of {@link com.epam.ta.reportportal.entity.item.TestItem}
	 * with {@link TestItemIssueGroup#TO_INVESTIGATE} for {@link com.epam.ta.reportportal.entity.launch.Launch} with provided ID.
	 * Every matched {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate} will be attached
	 * to the {@link com.epam.ta.reportportal.entity.item.TestItem}
	 * using {@link com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItem} relation
	 *
	 * @param launch {@link com.epam.ta.reportportal.entity.launch.Launch}, which {@link com.epam.ta.reportportal.entity.item.TestItem}
	 *               should be analyzed
	 */
	void analyzeTestItems(Launch launch);
}
