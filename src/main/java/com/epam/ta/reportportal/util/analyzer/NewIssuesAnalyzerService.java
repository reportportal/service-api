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
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexTestItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link INewIssuesAnalyzer}.
 *
 * @author Ivan Sharamet
 */
@Service("newAnalyzerService")
public class NewIssuesAnalyzerService implements INewIssuesAnalyzer {

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

        List<IndexTestItem> rqTestItems = new ArrayList<>();

        for (TestItem testItem : testItems) {
            List<Log> logs = logRepository.findByTestItemRef(testItem.getId());
            rqTestItems.add(IndexTestItem.fromTestItem(testItem, logs));
        }

        if (!rqTestItems.isEmpty()) {
            IndexLaunch rqLaunch = new IndexLaunch();
            rqLaunch.setLaunchId(launch.getId());
            rqLaunch.setLaunchName(launch.getName());
            rqLaunch.setTestItems(rqTestItems);

            rs = analyzerServiceClient.analyze(rqLaunch);
        }

        return rs;
    }

    private List<TestItem> updateTestItems(IndexLaunch rs, List<TestItem> testItems) {
        for (IndexTestItem rsTestItem : rs.getTestItems()) {
            if (rsTestItem.getIssueType() != null){
                for (TestItem testItem : testItems) {
                    if (testItem.getId().equals(rsTestItem.getTestItemId())) {
                        testItem.setIssue(new TestItemIssue(rsTestItem.getIssueType(), DEFAULT_ISSUE_DESCRIPTION));
                        break;
                    }
                }
            }
        }
        return testItems;
    }
}
