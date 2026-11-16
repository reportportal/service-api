/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.widget.util;

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.TOTAL;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Constants class holding compiled regex patterns for validating {@link Widget#contentFields}.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class ContentFieldPatternConstants {

  private static final String CONTENT_FIELD_SPLITTER = "\\$";

  static {

    EXECUTIONS_REGEX =
        "^" + "statistics" + CONTENT_FIELD_SPLITTER + EXECUTIONS_KEY + CONTENT_FIELD_SPLITTER + "("
            + Arrays.stream(
                StatusEnum.values()).map(StatusEnum::getExecutionCounterField)
            .collect(Collectors.joining("|")) + "|" + TOTAL + ")" + "$";

    DEFECTS_REGEX =
        "^" + "statistics" + CONTENT_FIELD_SPLITTER + DEFECTS_KEY + CONTENT_FIELD_SPLITTER + "("
            + Arrays.stream(
                TestItemIssueGroup.values()).map(ig -> ig.getValue().toLowerCase())
            .collect(Collectors.joining("|")) + ")"
            + CONTENT_FIELD_SPLITTER + "[\\w\\d]+" + "$";
  }

  /*
    ^statistics\\$executions\\$total$
   */
  public static final String EXECUTIONS_TOTAL_REGEX =
      "^" + "statistics" + CONTENT_FIELD_SPLITTER + EXECUTIONS_KEY + CONTENT_FIELD_SPLITTER + TOTAL
          + "$";

  /*
    ^statistics\\$executions\\$(passed|failed|skipped|total)$
   */
  public static final String EXECUTIONS_REGEX;

  /*
    ^statistics\\$executions\\$failed$
   */
  public static final String EXECUTIONS_FAILED_REGEX =
      "^" + "statistics" + CONTENT_FIELD_SPLITTER + EXECUTIONS_KEY + CONTENT_FIELD_SPLITTER
          + StatusEnum.FAILED.getExecutionCounterField() + "$";

  /*
    ^statistics\\$defects\\$(automation_bug|product_bug|no_defect|system_issue|to_investigate)\\$[\\w\\d]+$
   */
  public static final String DEFECTS_REGEX;

  /*
    ((^statistics\\$defects\\$(automation_bug|product_bug|no_defect|system_issue|to_investigate)\\$[\\w\\d]+$)|(^statistics\\$executions\\$(passed|failed|skipped|total)$))
   */
  public static final String COMBINED_CONTENT_FIELDS_REGEX =
      "(" + DEFECTS_REGEX + ")" + "|" + "(" + EXECUTIONS_REGEX + ")";

  private ContentFieldPatternConstants() {
    //static only
  }

}
