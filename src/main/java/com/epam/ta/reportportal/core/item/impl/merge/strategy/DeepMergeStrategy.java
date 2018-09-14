package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author Ivan Budaev
 */
public class DeepMergeStrategy extends AbstractSuiteMergeStrategy {

	@Autowired
	public DeepMergeStrategy(TestItemRepository testItemRepository) {
		super(testItemRepository);
	}

	@Override
	protected void mergeAllChildItems(TestItem testItemParent) {
		testItemRepository.selectAllDescendantsWithChildren(testItemParent.getItemId())
				.stream()
				.collect(groupingBy(TestItem::getUniqueId))
				.entrySet()
				.stream()
				.map(Map.Entry::getValue)
				.filter(items -> items.size() > 1)
				.forEach(items -> mergeTestItems(items.get(0), items.subList(1, items.size())));
	}

	@Override
	public boolean isTestItemAcceptableToMerge(TestItem item) {
		//DeepMerge special condition already implemented in the database query:
		return true;
	}
}
