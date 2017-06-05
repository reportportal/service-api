package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.issue.Issue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TestItemConverter {
    private TestItemConverter() {
        //static only
    }

    public static final Function<TestItem, TestItemResource> TO_RESOURCE = item -> {
        TestItemResource resource = new TestItemResource();
        if (Optional.ofNullable(item).isPresent()) {
            resource.setDescription(item.getItemDescription());
            resource.setTags(item.getTags());
            resource.setEndTime(item.getEndTime());
            resource.setItemId(item.getId());

            TestItemIssue testItemIssue = item.getIssue();
            if (null != testItemIssue) {
                Issue issue = new Issue();
                issue.setIssueType(testItemIssue.getIssueType());
                issue.setComment(testItemIssue.getIssueDescription());
                Set<TestItemIssue.ExternalSystemIssue> externalSystemIssues = testItemIssue.getExternalSystemIssues();
                if (null != externalSystemIssues) {
                    Set<Issue.ExternalSystemIssue> issuesResource = externalSystemIssues.stream().map(externalSystemIssue -> {
                        Issue.ExternalSystemIssue issueResource = new Issue.ExternalSystemIssue();
                        issueResource.setSubmitDate(externalSystemIssue.getSubmitDate());
                        issueResource.setTicketId(externalSystemIssue.getTicketId());
                        issueResource.setSubmitter(externalSystemIssue.getSubmitter());
                        issueResource.setExternalSystemId(externalSystemIssue.getExternalSystemId());
                        issueResource.setUrl(externalSystemIssue.getUrl());
                        return issueResource;
                    }).collect(Collectors.toSet());
                    issue.setExternalSystemIssues(issuesResource);
                }
                resource.setIssue(issue);
            }

            resource.setName(item.getName());
            resource.setStartTime(item.getStartTime());
            resource.setStatus(item.getStatus() != null ? item.getStatus().toString() : null);
            resource.setType(item.getType() != null ? item.getType().name() : null);
            resource.setParent(item.getParent() != null ? item.getParent() : null);
            resource.setHasChilds(item.hasChilds());
            //resource.setLaunchStatus(launchStatus);
            resource.setLaunchId(item.getLaunchRef());

            Statistics statistics = item.getStatistics();
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
        }
        return resource;
    };
}
