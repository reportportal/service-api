package com.epam.ta.reportportal.core.pattern;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PatternAnalyzer {

	/**
	 * Analyze by {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate#value}
	 * all {@link com.epam.ta.reportportal.entity.log.Log#logMessage} of {@link com.epam.ta.reportportal.entity.item.TestItem}
	 * with {@link TestItemIssueGroup#TO_INVESTIGATE} for {@link com.epam.ta.reportportal.entity.launch.Launch} with provided ID
	 * on {@link com.epam.ta.reportportal.entity.project.Project} with provided ID.
	 * Every matched {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate} will be attached
	 * to the {@link com.epam.ta.reportportal.entity.item.TestItem}
	 * using {@link com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItem} relation
	 *
	 * @param launchId  {@link com.epam.ta.reportportal.entity.launch.Launch#id}, which {@link com.epam.ta.reportportal.entity.item.TestItem}
	 *                  should be analyzed
	 * @param projectId {@link com.epam.ta.reportportal.entity.project.Project#id}, which launch's items should be analyzed
	 */
	void analyzeTestItems(Long launchId, Long projectId);
}
