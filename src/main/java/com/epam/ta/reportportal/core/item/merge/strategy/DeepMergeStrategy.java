package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class DeepMergeStrategy extends AbstractSuiteMergeStrategy {

	@Autowired
	public DeepMergeStrategy(TestItemRepository testItemRepository) {
		super(testItemRepository);
	}

	@Override
	public TestItem mergeTestItems(TestItem itemTarget, List<TestItem> items) {
		return moveAllChildTestItems(itemTarget, items);
	}

	@Override
	protected void mergeAllChildItems(TestItem testItemParent) {
		testItemRepository.findAllDescendants(testItemParent.getId())
				.stream()
				.filter(this::isTestItemAcceptableToMerge)
				.collect(groupingBy(TestItem::getUniqueId))
				.entrySet()
				.stream()
				.map(Map.Entry::getValue)
				.filter(items -> items.size() > 1)
				.forEach(items -> moveAllChildTestItems(items.get(0), items.subList(1, items.size())));
	}

	@Override
	public boolean isTestItemAcceptableToMerge(TestItem item) {
		return item.hasChilds();
	}
}
