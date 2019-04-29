package com.epam.ta.reportportal.core.pattern.selector;

import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
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
	 * @param launchId        {@link com.epam.ta.reportportal.entity.launch.Launch#id}
	 * @param issueGroup      {@link IssueGroup}
	 * @param patternTemplate {@link PatternTemplate}
	 * @return {@link PatternTemplateTestItemPojo} that contains item ID, which was matched by pattern and ID of the matched pattern
	 */
	List<PatternTemplateTestItemPojo> selectItemsByPattern(Long launchId, IssueGroup issueGroup, PatternTemplate patternTemplate);
}
