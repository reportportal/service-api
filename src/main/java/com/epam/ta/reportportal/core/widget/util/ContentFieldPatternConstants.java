package com.epam.ta.reportportal.core.widget.util;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * <p>
 * Regex patterns for @{@link com.epam.ta.reportportal.entity.widget.Widget#contentFields} validation
 */
public final class ContentFieldPatternConstants {

	private static final String CONTENT_FIELD_SPLITTER = "\\$";

	static {

		EXECUTIONS_REGEX = "^" + "statistics" + CONTENT_FIELD_SPLITTER + EXECUTIONS_KEY + CONTENT_FIELD_SPLITTER + "(" + Arrays.stream(
				StatusEnum.values()).map(StatusEnum::getExecutionCounterField).collect(Collectors.joining("|")) + "|" + TOTAL + ")" + "$";

		DEFECTS_REGEX = "^" + "statistics" + CONTENT_FIELD_SPLITTER + DEFECTS_KEY + CONTENT_FIELD_SPLITTER + "(" + Arrays.stream(
				TestItemIssueGroup.values()).map(ig -> ig.getValue().toLowerCase()).collect(Collectors.joining("|")) + ")"
				+ CONTENT_FIELD_SPLITTER + "[\\w\\d]+" + "$";
	}

	/*
		^statistics\\$executions\\$total$
	 */
	public static final String EXECUTIONS_TOTAL_REGEX =
			"^" + "statistics" + CONTENT_FIELD_SPLITTER + EXECUTIONS_KEY + CONTENT_FIELD_SPLITTER + TOTAL + "$";

	/*
		^statistics\\$executions\\$(passed|failed|skipped|total)$
	 */
	public static final String EXECUTIONS_REGEX;

	/*
		^statistics\\$defects\\$(automation_bug|product_bug|no_defect|system_issue|to_investigate)\\$[\\w\\d]+$
	 */
	public static final String DEFECTS_REGEX;

	/*
		((^statistics\\$defects\\$(automation_bug|product_bug|no_defect|system_issue|to_investigate)\\$[\\w\\d]+$)|(^statistics\\$executions\\$(passed|failed|skipped|total)$))
	 */
	public static final String COMBINED_CONTENT_FIELDS_REGEX = "(" + DEFECTS_REGEX + ")" + "|" + "(" + EXECUTIONS_REGEX + ")";

	private ContentFieldPatternConstants() {
		//static only
	}

}
