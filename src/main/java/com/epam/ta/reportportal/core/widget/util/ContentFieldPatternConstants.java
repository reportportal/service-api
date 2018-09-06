package com.epam.ta.reportportal.core.widget.util;

import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.PASSED;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;

/**
 * @author Ivan Budaev
 */
public final class ContentFieldPatternConstants {

	private static final String CONTENT_FIELD_SPLITTER = "\\$";

	public static final String EXECUTIONS_TOTAL_REGEX =
			"^" + "statistics" + CONTENT_FIELD_SPLITTER + EXECUTIONS_KEY + CONTENT_FIELD_SPLITTER + TOTAL + "$";

	public static final String EXECUTIONS_REGEX =
			"^" + "statistics" + CONTENT_FIELD_SPLITTER + EXECUTIONS_KEY + CONTENT_FIELD_SPLITTER + "(" + PASSED.getExecutionCounterField()
					+ "|" + FAILED.getExecutionCounterField() + "|" + SKIPPED.getExecutionCounterField() + "|" + TOTAL + ")" + "$";

	public static final String DEFECTS_REGEX =
			"^" + "statistics" + CONTENT_FIELD_SPLITTER + DEFECTS_KEY + CONTENT_FIELD_SPLITTER + "(" + AUTOMATION_BUG.getValue()
					.toLowerCase() + "|" + PRODUCT_BUG.getValue().toLowerCase() + "|" + NO_DEFECT.getValue().toLowerCase() + "|"
					+ SYSTEM_ISSUE.getValue().toLowerCase() + "|" + TO_INVESTIGATE.getValue().toLowerCase() + ")" + CONTENT_FIELD_SPLITTER
					+ "[\\w\\d]+" + "$";

	public static final String COMBINED_CONTENT_FIELDS_REGEX =
			"(" + "(" + DEFECTS_REGEX + ")" + "|" + "(" + EXECUTIONS_REGEX + ")" + ")" + "$";

	private ContentFieldPatternConstants() {
		//static only
	}

}
