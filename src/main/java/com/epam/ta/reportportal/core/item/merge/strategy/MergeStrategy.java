package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.List;

public interface MergeStrategy {
    TestItem mergeTestItems(TestItem itemTarget, List<TestItem> items);
}
