package com.epam.ta.reportportal.core.pattern.impl;

import com.epam.ta.reportportal.core.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.core.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.dao.IssueGroupRepository;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PatternAnalyzerImpl implements PatternAnalyzer {

	private final IssueGroupRepository issueGroupRepository;

	private final PatternTemplateRepository patternTemplateRepository;

	private final Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping;

	private final TaskExecutor patternAnalysisTaskExecutor;

	@Autowired
	public PatternAnalyzerImpl(IssueGroupRepository issueGroupRepository, PatternTemplateRepository patternTemplateRepository,
			@Qualifier("patternAnalysisSelectorMapping") Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping,
			TaskExecutor patternAnalysisTaskExecutor) {
		this.issueGroupRepository = issueGroupRepository;
		this.patternTemplateRepository = patternTemplateRepository;
		this.patternAnalysisSelectorMapping = patternAnalysisSelectorMapping;
		this.patternAnalysisTaskExecutor = patternAnalysisTaskExecutor;
	}

	@Override
	public void analyzeTestItems(Launch launch) {

		IssueGroup issueGroup = issueGroupRepository.findByTestItemIssueGroup(TestItemIssueGroup.TO_INVESTIGATE);

		patternTemplateRepository.findAllByProjectIdAndEnabled(launch.getProjectId(), true)
				.forEach(patternTemplate -> patternAnalysisTaskExecutor.execute(() -> {
					List<PatternTemplateTestItemPojo> patternTemplateTestItems = patternAnalysisSelectorMapping.get(patternTemplate.getTemplateType())
							.selectItemsByPattern(launch.getId(), issueGroup, patternTemplate);
					patternTemplateRepository.saveInBatch(patternTemplateTestItems);

				}));
	}

}
