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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class LaunchResourceBuilder extends Builder<LaunchResource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchResourceBuilder.class);

    public LaunchResourceBuilder addLaunch(Launch launch) {
        Preconditions.checkNotNull(launch);
        LaunchResource resource = getObject();
        resource.setLaunchId(launch.getId());
        resource.setName(launch.getName());
        resource.setNumber(launch.getNumber());
        resource.setDescription(launch.getDescription());
        resource.setStatus(launch.getStatus() == null ? null : launch.getStatus().toString());
        resource.setStartTime(launch.getStartTime());
        resource.setEndTime(launch.getEndTime());
        resource.setTags(launch.getTags());
        resource.setMode(launch.getMode());
        resource.setApproximateDuration(launch.getApproximateDuration());
        resource.setIsProcessing(true);
        resource.setOwner(launch.getUserRef());
        Statistics statistics = launch.getStatistics();
        if (statistics != null) {
            com.epam.ta.reportportal.ws.model.statistics.Statistics statisticsCounters = new com.epam.ta.reportportal.ws.model.statistics.Statistics();
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
            resource.setStatistics(statisticsCounters);
        }
        return this;
    }

    @Override
    protected LaunchResource initObject() {
        return new LaunchResource();
    }
}