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

package com.epam.ta.reportportal.core.analyzer;

import com.epam.ta.reportportal.database.entity.AnalyzeMode;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.List;

/**
 * Service for issue type analysis based on historical data.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
public interface IIssuesAnalyzer {

	/**
	 * Analyze history to find similar issues and updates items if some were found
	 * Indexes investigated issues as well.
	 *
	 * @param launch    Initial launch for history
	 * @param testItems - current test items with failed status and issue
	 * @param mode      - Analyze mode
	 */
	void analyze(Launch launch, List<TestItem> testItems, AnalyzeMode mode);

	/**
	 * Checks if any analyzer is available
	 *
	 * @return <code>true</code> if some exists
	 */
	boolean hasAnalyzers();
}
