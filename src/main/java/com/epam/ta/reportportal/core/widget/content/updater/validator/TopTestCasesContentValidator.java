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

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
        validateWidgetLimit(limit);
	}

  /**
   * Validate provided content fields. For current widget it should be only one field specified in
   * content fields. Example is 'executions$failed', so widget would be created by 'failed'
   * criteria.
   *
   * @param contentFields List of provided content.
   */
  private void validateContentFields(List<String> contentFields) {
    BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
        .verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
    BusinessRule.expect(contentFields.size(), Predicate.isEqual(1))
        .verify(ErrorType.BAD_REQUEST_ERROR, "Only one content field could be specified.");
  }

    /**
     * Validate provided widget launches count. For current widget launches count should in the range from 2 to 100.
     *
     * @param limit launches count.
     */
    private void validateWidgetLimit(int limit) {
        BusinessRule.expect(limit > 100 || limit < 2 , equalTo(false))
                .verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,  "Items count should have value from 2 to 100.");
    }
}
