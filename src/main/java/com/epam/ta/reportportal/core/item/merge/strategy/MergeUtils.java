package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;

public class MergeUtils {
    public static boolean isTestItemStatusIsZeroLevel(TestItem item) {
        return item.getType() == TestItemType.SUITE;
    }

    public static boolean isTestItemStatusIsNotZeroLevel(TestItem item) {
        return item.getType() != TestItemType.SUITE;
    }
}
