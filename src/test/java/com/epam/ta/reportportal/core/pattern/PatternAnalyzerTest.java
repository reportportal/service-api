package com.epam.ta.reportportal.core.pattern;

import com.epam.ta.reportportal.core.pattern.impl.PatternAnalyzerImpl;
import com.epam.ta.reportportal.core.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.core.pattern.selector.impl.StringPartPatternAnalysisSelector;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PatternAnalyzerTest {

	private final PatternAnalysisSelector stringSelector = mock(StringPartPatternAnalysisSelector.class);

	private final IssueTypeRepository issueTypeRepository = mock(IssueTypeRepository.class);
	private final PatternTemplateRepository patternTemplateRepository = mock(PatternTemplateRepository.class);

	private final TaskExecutor taskExecutor = mock(TaskExecutor.class);

	private final IssueType issueType = mock(IssueType.class);

	private final Map<PatternTemplateType, PatternAnalysisSelector> analysisSelectorMapping = mock(Map.class);
	private final PatternAnalyzer patternAnalyzer = new PatternAnalyzerImpl(issueTypeRepository,
			patternTemplateRepository,
			analysisSelectorMapping,
			taskExecutor
	);

	@Test
	void analyzeTestItems() {
		when(issueTypeRepository.findByLocator(any(String.class))).thenReturn(ofNullable(issueType));
		when(patternTemplateRepository.findAllByProjectIdAndEnabled(1L, true)).thenReturn(getPatternTemplates());

		when(analysisSelectorMapping.get(PatternTemplateType.STRING)).thenReturn(stringSelector);

		when(stringSelector.selectItemsByPattern(any(Long.class), any(IssueType.class), any(PatternTemplate.class))).thenReturn(
				getPatternTemplateTestItemPojos(1L));
		when(stringSelector.selectItemsByPattern(any(Long.class), any(IssueType.class), any(PatternTemplate.class))).thenReturn(
				getPatternTemplateTestItemPojos(2L));
		doNothing().when(taskExecutor).execute(any());

		patternAnalyzer.analyzeTestItems(1L, 1L);
	}

	private List<PatternTemplate> getPatternTemplates() {

		return Lists.newArrayList(getPatternTemplate(1L, "name", "value", PatternTemplateType.STRING),
				getPatternTemplate(2L, "name1", "value1", PatternTemplateType.REGEX)
		);
	}

	private PatternTemplate getPatternTemplate(Long id, String name, String value, PatternTemplateType type) {
		PatternTemplate patternTemplate = new PatternTemplate();
		patternTemplate.setId(id);
		patternTemplate.setName(name);
		patternTemplate.setValue(value);
		patternTemplate.setEnabled(true);
		patternTemplate.setTemplateType(type);
		patternTemplate.setProjectId(1L);
		return patternTemplate;
	}

	private List<PatternTemplateTestItemPojo> getPatternTemplateTestItemPojos(Long patternId) {

		return Lists.newArrayList(new PatternTemplateTestItemPojo(patternId, 1L), new PatternTemplateTestItemPojo(patternId, 2L));
	}
}