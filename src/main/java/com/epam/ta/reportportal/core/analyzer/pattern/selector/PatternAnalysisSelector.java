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

package com.epam.ta.reportportal.core.analyzer.pattern.selector;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PatternAnalysisSelector {

	/**
	 * Select {@link PatternTemplateTestItemPojo} matched by {@link PatternTemplate#value}
	 *
	 * @param filter          {@link Queryable}
	 * @param patternTemplate {@link PatternTemplate}
	 * @return {@link PatternTemplateTestItemPojo} that contains item ID, which was matched by pattern and ID of the matched pattern
	 */
	List<PatternTemplateTestItemPojo> selectItemsByPattern(Queryable filter, PatternTemplate patternTemplate);
}
