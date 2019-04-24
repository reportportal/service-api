package com.epam.ta.reportportal.core.pattern;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PatternAnalyzer {

	/**
	 * @param launchId
	 * @param projectId
	 */
	void analyzeTestItems(Long launchId, Long projectId);
}
