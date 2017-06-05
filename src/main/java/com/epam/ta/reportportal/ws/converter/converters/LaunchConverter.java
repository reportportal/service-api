package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.util.analyzer.IssuesAnalyzerService;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public final class LaunchConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchConverter.class);

    private LaunchConverter() {
        //static only
    }

    public static final Function<Launch, LaunchResource> TO_RESOURCE = launch -> {
        LaunchResource resource = new LaunchResource();
        if (Optional.ofNullable(launch).isPresent()) {
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
            try {
                IssuesAnalyzerService analyzeService = new IssuesAnalyzerService();
                if (null != analyzeService.getProcessIds().get(launch.getId())) {
                    if ("started".equalsIgnoreCase(analyzeService.getProcessIds().get(launch.getId())))
                        resource.setIsProcessing(true);
                } else
                    resource.setIsProcessing(false);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                resource.setIsProcessing(false);
            }
            resource.setOwner(launch.getUserRef());
            Statistics statistics = launch.getStatistics();
            if (statistics != null) {
                com.epam.ta.reportportal.ws.model.statistics.Statistics statisticsCounters =
                        new com.epam.ta.reportportal.ws.model.statistics.Statistics();
                ExecutionCounter executionCounter = statistics.getExecutionCounter();
                if (executionCounter != null) {
                    com.epam.ta.reportportal.ws.model.statistics.ExecutionCounter execution =
                            new com.epam.ta.reportportal.ws.model.statistics.ExecutionCounter();
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
