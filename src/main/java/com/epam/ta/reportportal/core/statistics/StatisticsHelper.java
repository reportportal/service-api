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

import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;

/**
 * Utility class to process statistics data and produce output, based on it
 *
 * @author Dzianis_Shlychkou
 */
public class StatisticsHelper {

	private StatisticsHelper() {
	}

	public static Status getStatusFromStatistics(Statistics statistics) {

		ExecutionCounter executionCounter = statistics.getExecutionCounter();
		Integer toInvestigate = statistics.getIssueCounter().getToInvestigateTotal();
		if (executionCounter.getFailed() > 0 || executionCounter.getSkipped() > 0 || toInvestigate > 0) {
			return Status.FAILED;
		} else {
			return Status.PASSED;
		}
	}

}