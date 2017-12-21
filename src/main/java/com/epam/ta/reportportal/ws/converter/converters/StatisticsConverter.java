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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;

import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public final class StatisticsConverter {

	private StatisticsConverter() {
		//static only
	}

	public static final Function<Statistics, com.epam.ta.reportportal.ws.model.statistics.Statistics> TO_RESOURCE = statistics -> {
		com.epam.ta.reportportal.ws.model.statistics.Statistics statisticsCounters = new com.epam.ta.reportportal.ws.model.statistics.Statistics();
		if (statistics != null) {
			ExecutionCounter executionCounter = statistics.getExecutionCounter();
			if (executionCounter != null) {
				com.epam.ta.reportportal.ws.model.statistics.ExecutionCounter execution = new com.epam.ta.reportportal.ws.model.statistics.ExecutionCounter();
				execution.setTotal(executionCounter.getTotal().toString());
				execution.setPassed(executionCounter.getPassed().toString());
				execution.setFailed(executionCounter.getFailed().toString());
				execution.setSkipped(executionCounter.getSkipped().toString());
				statisticsCounters.setExecutions(execution);
			}
			IssueCounter issueCounter = statistics.getIssueCounter();
			if (issueCounter != null) {
				com.epam.ta.reportportal.ws.model.statistics.IssueCounter issues = new com.epam.ta.reportportal.ws.model.statistics.IssueCounter();
				issues.setProductBug(issueCounter.getProductBug());
				issues.setSystemIssue(issueCounter.getSystemIssue());
				issues.setAutomationBug(issueCounter.getAutomationBug());
				issues.setToInvestigate(issueCounter.getToInvestigate());
				issues.setNoDefect(issueCounter.getNoDefect());
				statisticsCounters.setDefects(issues);
			}
		}
		return statisticsCounters;
	};
}
