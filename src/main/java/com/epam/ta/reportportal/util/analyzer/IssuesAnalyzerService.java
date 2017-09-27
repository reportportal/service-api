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

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexTestItem;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link IIssuesAnalyzer}.
 *
 * @author Ivan Sharamet
 */
@Service("analyzerService")
public class IssuesAnalyzerService implements IIssuesAnalyzer {

	private static final String DEFAULT_ISSUE_DESCRIPTION = "";

	@Autowired
	private AnalyzerServiceClient analyzerServiceClient;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private LogRepository logRepository;

	@Override
	public List<TestItem> analyze(String launchId, List<TestItem> testItems) {
		Launch launch = launchRepository.findEntryById(launchId);
		if (launch != null) {
			IndexLaunch rs = analyze(launch, testItems);
			if (rs != null) {
				updateTestItems(rs, testItems);
			}
		}
		return testItems;
	}

	private IndexLaunch analyze(Launch launch, List<TestItem> testItems) {
		IndexLaunch rs = null;

		List<IndexTestItem> rqTestItems = testItems.stream()
				.map(it -> IndexTestItem.fromTestItem(it, logRepository.findByTestItemRef(it.getId())))
				.filter(it -> !CollectionUtils.isEmpty(it.getLogs()))
				.collect(Collectors.toList());

		if (!rqTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProject(launch.getProjectRef());
			rqLaunch.setTestItems(rqTestItems);

			rs = analyzerServiceClient.analyze(rqLaunch);
		}

		return rs;
	}

	private List<TestItem> updateTestItems(IndexLaunch rs, List<TestItem> testItems) {
		rs.getTestItems()
				.stream()
				.filter(it -> it.getIssueType() != null)
				.forEach(indexTestItem -> testItems.stream()
						.filter(testItem -> testItem.getId().equals(indexTestItem.getTestItemId()))
						.findFirst()
						.ifPresent(it -> it.setIssue(new TestItemIssue(indexTestItem.getIssueType(), DEFAULT_ISSUE_DESCRIPTION))));
		return testItems;
	}
}
