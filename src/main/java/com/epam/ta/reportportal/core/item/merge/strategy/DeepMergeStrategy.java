package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
		List<TestItem> childItems = testItemRepository.findAllDescendants(testItemParent.getId());
		List<TestItem> testItems = childItems.stream().filter(this::isTestItemAcceptableToMerge).collect(toList());

		testItems.stream()
				.collect(groupingBy(TestItem::getType, groupingBy(TestItem::getName)))
				.entrySet()
				.stream()
				.map(Map.Entry::getValue)
				.collect(HashMap<String, List<TestItem>>::new, HashMap::putAll, HashMap::putAll)
				.entrySet()
				.stream()
				.map(Map.Entry::getValue)
				.collect(toList())
				.forEach(items -> moveAllChildTestItems(items.get(0), items.subList(1, items.size())));
	}

	@Override
	public boolean isTestItemAcceptableToMerge(TestItem item) {
		return item.hasChilds();
	}
}
