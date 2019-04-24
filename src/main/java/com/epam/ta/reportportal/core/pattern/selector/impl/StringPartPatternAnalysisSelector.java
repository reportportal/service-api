package com.epam.ta.reportportal.core.pattern.selector.impl;

import com.epam.ta.reportportal.core.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class StringPartPatternAnalysisSelector implements PatternAnalysisSelector {

	private final TestItemRepository testItemRepository;

	@Autowired
	public StringPartPatternAnalysisSelector(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public List<PatternTemplateTestItemPojo> selectItemsByPattern(Long launchId, IssueType issueType, PatternTemplate patternTemplate) {
		return testItemRepository.selectIdsByLaunchIdAndIssueTypeIdAndLogLevelAndLogMessageStringPattern(launchId,
				issueType.getId(),
				LogLevel.ERROR.toInt(),
				patternTemplate.getValue()
		).stream().map(itemId -> new PatternTemplateTestItemPojo(patternTemplate.getId(), itemId)).collect(Collectors.toList());
	}
}
