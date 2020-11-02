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

package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

/**
 * Content loader for {@link com.epam.ta.reportportal.entity.widget.WidgetType#TOP_TEST_CASES}
 *
 * @author Pavel Bortnik
 */
@Service
public class TopTestCasesContentValidator implements WidgetValidatorStrategy {

	@Override
	public void validate(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions, int limit) {
		validateContentFields(contentFields);
	}

	/**
	 * Validate provided content fields. For current widget it should be only one field specified in content fields.
	 * Example is 'executions$failed', so widget would be created by 'failed' criteria.
	 *
	 * @param contentFields List of provided content.
	 */
	private void validateContentFields(List<String> contentFields) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(contentFields.size(), Predicate.isEqual(1))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Only one content field could be specified.");
	}
}
