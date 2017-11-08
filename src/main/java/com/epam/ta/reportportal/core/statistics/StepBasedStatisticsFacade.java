/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
import org.springframework.stereotype.Service;

/**
 * Statistics facade for case when we need to calculate statistics starting for lower level excluding (not for before and after methods)
 *
 * @author Andrei Varabyeu
 */
@Service
public class StepBasedStatisticsFacade extends StatisticsFacadeImpl implements StatisticsFacade {

	@Override
	public TestItem updateExecutionStatistics(final TestItem testItem) {
		if (!testItem.hasChilds() && testItem.getType().awareStatistics()) {
			return super.updateExecutionStatistics(testItem);
		} else {
			return testItem;
		}
	}

	@Override
	public TestItem resetExecutionStatistics(TestItem testItem) {
		if (testItem.getType().awareStatistics()) {
			return super.resetExecutionStatistics(testItem);
		} else {
			return testItem;
		}
	}

	@Override
	public TestItem deleteExecutionStatistics(TestItem testItem) {
		if (testItem.getType().awareStatistics()) {
			return super.deleteExecutionStatistics(testItem);
		} else {
			return testItem;
		}
	}

	@Override
	public boolean awareIssue(TestItem testItem) {
		return !testItem.hasChilds();
	}
}
