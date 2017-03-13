/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import org.springframework.stereotype.Service;

/**
 * BDD Optimized statistics calculation strategy
 * Do not calculates stats for step-level
 *
 * @author Andrei Varabyeu
 * @see TestItemType#level
 */
@Service
public class TestBasedStatisticsFacade extends StatisticsFacadeImpl implements StatisticsFacade {

    @Override
    public TestItem updateExecutionStatistics(final TestItem testItem) {
        TestItemType type = testItem.getType();
        if (type.awareStatistics() && type.sameLevel(TestItemType.TEST)) {
            return super.updateExecutionStatistics(testItem);
        } else {
            return testItem;
        }
    }

    @Override
    public TestItem resetExecutionStatistics(TestItem testItem) {
        TestItemType type = testItem.getType();
        if (type.awareStatistics() && type.sameLevel(TestItemType.TEST)) {
            return super.resetExecutionStatistics(testItem);
        } else {
            return testItem;
        }
    }

    @Override
    public TestItem deleteExecutionStatistics(TestItem testItem) {
        TestItemType type = testItem.getType();
        if (type.awareStatistics() && type.sameLevel(TestItemType.TEST)) {
            return super.deleteExecutionStatistics(testItem);
        } else {
            return testItem;
        }
    }

    @Override
    public TestItem updateIssueStatistics(TestItem testItem) {
        TestItemType type = testItem.getType();
        if (type.awareStatistics() && type.sameLevel(TestItemType.TEST)) {
            return super.updateIssueStatistics(testItem);
        } else {
            return testItem;
        }
    }

    @Override
    public TestItem resetIssueStatistics(TestItem testItem) {
        TestItemType type = testItem.getType();
        if (type.awareStatistics() && type.sameLevel(TestItemType.TEST)) {
            return super.resetIssueStatistics(testItem);
        } else {
            return testItem;
        }
    }

    @Override
    public TestItem deleteIssueStatistics(TestItem testItem) {
        TestItemType type = testItem.getType();
        if (type.awareStatistics() && type.sameLevel(TestItemType.TEST)) {
            return super.deleteIssueStatistics(testItem);
        } else {
            return testItem;
        }
    }
}
