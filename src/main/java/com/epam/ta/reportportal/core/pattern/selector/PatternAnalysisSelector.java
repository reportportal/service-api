package com.epam.ta.reportportal.core.pattern.selector;

import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PatternAnalysisSelector {

	/**
	 * @param launchId
	 * @param issueType
	 * @param patternTemplate
	 * @return
	 */
	List<PatternTemplateTestItemPojo> selectItemsByPattern(Long launchId, IssueType issueType, PatternTemplate patternTemplate);
}
