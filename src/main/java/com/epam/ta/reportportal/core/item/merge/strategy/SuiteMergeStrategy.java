package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.core.item.merge.strategy.MergeUtils.isTestItemStatusIsZeroLevel;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SuiteMergeStrategy extends AbstractSuiteMergeStrategy {

    @Autowired
    public SuiteMergeStrategy(TestItemRepository testItemRepository) {
        super(testItemRepository);
    }

    @Override
    public TestItem mergeTestItems(TestItem itemTarget, List<TestItem> items) {
        return moveAllChildTestItems(itemTarget, items);
    }

    public boolean isTestItemAcceptableToMerge(TestItem item) {
        if (!isTestItemStatusIsZeroLevel(item)) {
            return false;
        }
        List<TestItem> childItems = testItemRepository.findAllDescendants(item.getId());
        List<TestItem> tests = childItems.stream().filter(MergeUtils::isTestItemStatusIsNotZeroLevel).collect(toList());
        Set<String> names = tests.stream().map(TestItem::getName).collect(toSet());
        if (names.size() != tests.size()) {
            return false;
        }
        return true;
    }
}
