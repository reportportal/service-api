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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.core.analyzer.IssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Default implementation of {@link IssuesAnalyzer}.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
@Service
public class IssuesAnalyzerServiceImpl implements IssuesAnalyzer {

	@Autowired
	private AnalyzerStatusCache analyzerStatusCache;

	@Autowired
	private LogRepository logRepository;

	@Override
	public void analyze(Launch launch, Project project, List<TestItem> testItems, AnalyzeMode mode) {
		if (launch != null) {
			analyzerStatusCache.analyzeStarted(launch.getId(), project.getName());
			List<IndexTestItem> indexTestItems = prepareItems(testItems);

		}
	}

	@Override
	public boolean hasAnalyzers() {
		return false;
	}

	/**
	 * Filter items with logs greater than {@link LogLevel#ERROR} level
	 * and convert them to {@link IndexTestItem} analyzer model
	 *
	 * @param testItems Test items for preparing
	 * @return Prepared items for analyzer
	 */
	private List<IndexTestItem> prepareItems(List<TestItem> testItems) {
		return testItems.stream()
				.map(it -> AnalyzerUtils.fromTestItem(it,
						logRepository.findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(singletonList(it.getItemId()),
								LogLevel.ERROR.toInt()
						)
				))
				.filter(it -> !isEmpty(it.getLogs()))
				.collect(Collectors.toList());
	}

	private IndexLaunch prepareLaunch(List<IndexTestItem> rqTestItems, Launch launch, Project project, AnalyzeMode analyzeMode) {
		if (!rqTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProject(project.getName());
			AnalyzerConfig analyzerConfig = AnalyzerConfigConverter.TO_RESOURCE.apply(project.getConfiguration().getAnalyzerConfig());
			//uses provided analyze mode because it could be run with another mode from launch view
			analyzerConfig.setAnalyzerMode(analyzeMode.getValue());

			rqLaunch.setAnalyzerConfig(analyzerConfig);
			rqLaunch.setTestItems(rqTestItems);
			return rqLaunch;
		}
		return null;
	}
}
