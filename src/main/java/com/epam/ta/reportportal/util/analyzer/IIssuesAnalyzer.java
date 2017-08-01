/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.util.analyzer;

import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.List;

/**
 * Service for issue type analysis based on historical data.
 *
 * @author Ivan Sharamet
 *
 */
public interface IIssuesAnalyzer {

    /**
     * Analyze history to find similar issues
     *
     * @param launchId
     *            ID of initial launch for history
     * @param testItems
     *            - current test items with failed\skipped status and issue
     *
     * @return list of test items with updated issue type
     */
    List<TestItem> analyze(String launchId, List<TestItem> testItems);
}
