package com.epam.ta.reportportal.core.pattern.impl;

import com.epam.ta.reportportal.core.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.core.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
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

	private final IssueTypeRepository issueTypeRepository;

	private final PatternTemplateRepository patternTemplateRepository;

	private final Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping;

	private final TaskExecutor patternAnalysisTaskExecutor;

	@Autowired
	public PatternAnalyzerImpl(IssueTypeRepository issueTypeRepository, PatternTemplateRepository patternTemplateRepository,
			@Qualifier("patternAnalysisSelectorMapping") Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping,
			TaskExecutor patternAnalysisTaskExecutor) {
		this.issueTypeRepository = issueTypeRepository;
		this.patternTemplateRepository = patternTemplateRepository;
		this.patternAnalysisSelectorMapping = patternAnalysisSelectorMapping;
		this.patternAnalysisTaskExecutor = patternAnalysisTaskExecutor;
	}

	@Override
	public void analyzeTestItems(Long launchId, Long projectId) {

		IssueType issueType = issueTypeRepository.findByLocator(TestItemIssueGroup.TO_INVESTIGATE.getLocator())
				.orElseThrow(() -> new ReportPortalException(ErrorType.ISSUE_TYPE_NOT_FOUND, TestItemIssueGroup.TO_INVESTIGATE.getValue()));

		patternTemplateRepository.findAllByProjectIdAndEnabled(projectId, true)
				.forEach(patternTemplate -> patternAnalysisTaskExecutor.execute(() -> {
					List<PatternTemplateTestItemPojo> patternTemplateTestItems = patternAnalysisSelectorMapping.get(patternTemplate.getTemplateType())
							.selectItemsByPattern(launchId, issueType, patternTemplate);
					patternTemplateRepository.saveInBatch(patternTemplateTestItems);

				}));
	}

}
