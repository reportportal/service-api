package com.epam.ta.reportportal.core.analyzer.pattern.selector.impl;

import com.epam.ta.reportportal.core.analyzer.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractPatternAnalysisSelector implements PatternAnalysisSelector {

	protected final TestItemRepository testItemRepository;

	public AbstractPatternAnalysisSelector(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	protected abstract List<Long> getItemsWithMatches(String pattern, Set<Long> itemIds);
	protected abstract List<Long> getItemsWithNestedStepsMatches(Long launchId, String pattern, List<Long> itemsWithNestedSteps);

	@Override
	public List<Long> selectItemsByPattern(Long launchId, Collection<Long> itemIds, String pattern) {
		final Set<Long> sourceIds = Sets.newHashSet(itemIds);
		final List<Long> itemsWithMatchedLogs = getItemsWithMatches(pattern, sourceIds);

		sourceIds.removeAll(itemsWithMatchedLogs);

		if (CollectionUtils.isNotEmpty(sourceIds)) {
			final List<Long> itemsWithNestedSteps = testItemRepository.selectIdsByHasDescendants(sourceIds);
			if (CollectionUtils.isNotEmpty(itemsWithNestedSteps)) {
				final List<Long> nestedStepsMatches = getItemsWithNestedStepsMatches(launchId, pattern, itemsWithNestedSteps);
				itemsWithMatchedLogs.addAll(nestedStepsMatches);
			}
		}

		return itemsWithMatchedLogs;
	}
}
