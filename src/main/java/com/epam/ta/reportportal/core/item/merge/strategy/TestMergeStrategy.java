package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.epam.ta.reportportal.core.item.merge.strategy.MergeUtils.isTestItemStatusIsZeroLevel;

public class TestMergeStrategy extends AbstractSuiteMergeStrategy {

    @Autowired
    public TestMergeStrategy(TestItemRepository testItemRepository) {
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
        for (TestItem testItem : childItems) {
            if (!isTestItemStatusIsZeroLevel(testItem)) {
                return false;
            }
        }
        return true;
    }
}
