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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.util.analyzer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.item.history.ITestItemsHistoryService;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

/**
 * Basic implementation of {@link IIssuesAnalyzer}
 *
 * @author Andrei_Ramanchuk
 *
 */
@Service("analyzerService")
public class IssuesAnalyzerService implements IIssuesAnalyzer {

	private static final Logger LOGGER = LoggerFactory.getLogger(IssuesAnalyzerService.class);
	private static final String MARKER = "AUTO-SYSTEM";
	private static final int MAXIMUM_SIZE = 10000;

	// Max time of cache item period
	private static final int CACHE_ITEM_LIVE = 1440;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private ITestItemsHistoryService historyServiceStrategy;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Value("${rp.issue.analyzer.rate}")
	private double acceptRate;

	/**
	 * Cache for processing launches with Auto-Analyzer
	 */
	private LoadingCache<String, String> processingIds;

	public IssuesAnalyzerService() {
		processingIds = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES)
				.build(new CacheLoader<String, String>() {
					@Override
					public String load(String key) {
						return "";
					}
				});
	}

	@Override
	public List<TestItem> collectPreviousIssues(int depth, String launchId, String projectName) {
		processingIds.put(launchId, CacheElementEnum.STARTED.name());
		List<Launch> launchHistory = historyServiceStrategy.loadLaunches(depth, launchId, projectName, false);
		return launchHistory.stream().flatMap(launch -> testItemRepository.findTestItemWithInvestigated(launch.getId()).stream())
				.collect(Collectors.toList());
	}

	@Override
	public void analyze(String launchId, List<TestItem> resources, List<TestItem> scope) {
		try {
			for (TestItem current : resources) {
				List<Double> curRate = Lists.newArrayList();
				TestItemIssue issue = null;
				boolean isInvestigated = false;
				List<Log> curItemErr = logRepository.findTestItemErrorLogs(current.getId());

				Launch launch = launchRepository.findOne(current.getLaunchRef());
				Project project = projectRepository.findOne(launch.getProjectRef());

				for (TestItem item : scope) {
				/*
				 * Avoid comparison with itself as investigated item during
				 * in_progress launch. Cause manually investigated item will be
				 * included in history of current one.
				 */
					if (item.getId().equalsIgnoreCase(current.getId()))
						continue;

					List<Log> errors = logRepository.findTestItemErrorLogs(item.getId());
					if (errors.size() == curItemErr.size()) {
						for (int i = 0; i < curItemErr.size(); i++) {
							String curMsg = curItemErr.get(i).getLogMsg().replaceAll("\\d+", "").replaceAll("\\s(at)\\s", "");
							String scopeMsg = errors.get(i).getLogMsg().replaceAll("\\d+", "").replaceAll("\\s(at)\\s", "");
						/*
						 * Get Levenshtein distance for two comparing log
						 * strings
						 */
							int maxString = Math.max(curMsg.length(), scopeMsg.length());
							int diff = StringUtils.getLevenshteinDistance(curMsg, scopeMsg);
						/*
						 * Store percentage of equality
						 */
							curRate.add(((double) (maxString - diff)) / maxString * 100);
						}
					}
					if (!curRate.isEmpty() && (this.mathMiddle(curRate) >= acceptRate)) {
						isInvestigated = true;
						issue = item.getIssue();
					/* Stop looping cause acceptable item found already. */
						break;
					} else
						curRate.clear();
				}

				if (isInvestigated) {
					TestItemIssue currentIssue = current.getIssue();
				/* If item was investigated till Launch finished. */
					if ((null != currentIssue.getExternalSystemIssues()) || (!currentIssue.getIssueType().equalsIgnoreCase(TestItemIssueType.TO_INVESTIGATE.getLocator()))
							|| (null != currentIssue.getIssueDescription())) {
						currentIssue.setIssueDescription(this.suggest(currentIssue.getIssueDescription(), issue, project.getConfiguration()));
						current.setIssue(currentIssue);
						testItemRepository.save(current);
					/* If system investigate item from scratch */
					} else {
						issue.setIssueDescription(this.mark(issue.getIssueDescription()));
						current = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
								.resetIssueStatistics(current);
						current.setIssue(issue);
						testItemRepository.save(current);
						statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy()).updateIssueStatistics(current);
					}
				}
			}
		} finally {
			analyzeFinished(launchId);
		}
	}

	@Override
	public boolean isPossible(String key) {
		String value = processingIds.getIfPresent(key);
		return Strings.isNullOrEmpty(value) || CacheElementEnum.FINISHED.name().equalsIgnoreCase(value);
	}

	@Override
	public void analyzeFinished(String key) {
		processingIds.put(key, CacheElementEnum.FINISHED.name());
		processingIds.invalidate(key);
	}

	@Override
	public boolean analyzeStarted(String key) {
		try {
			processingIds.put(key, CacheElementEnum.STARTED.name());
			return true;
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return false;
		}
	}

	/*
	 * Arithmetical Mean of list of double values
	 */
	private double mathMiddle(List<Double> rates) {
		double sum = 0;
		for (double i : rates)
			sum += i;
		return sum / rates.size();
	}

	/*
	 * Marker for issues which were auto-detected
	 */
	private String mark(String description) {
		StringBuilder builder = new StringBuilder(MARKER);
		return null != description ? builder.append("\n\n").append(this.markerNullifier(description)).toString() : builder.toString();
	}

	/*
	 * Add additional description for already investigated test-items during
	 * launch running
	 */
	private String suggest(String currentDescription, TestItemIssue previousIssue, Project.Configuration settings) {
		StringBuilder builder = new StringBuilder();
		if (null != currentDescription)
			builder.append(currentDescription);
		StringBuilder ticketIds = new StringBuilder();
		if (null != previousIssue.getExternalSystemIssues()) {
			for (TestItemIssue.ExternalSystemIssue externalSystemIssue : previousIssue.getExternalSystemIssues()) {
				ticketIds.append(externalSystemIssue.getTicketId()).append(",");
			}
			if (ticketIds.length() > 0)
				ticketIds.deleteCharAt(ticketIds.length() - 1);
			else
				ticketIds.append("");
		}
		builder.append("\n").append(MARKER).append("\n\n").append(" Similar issue has been found in history as:").append("\n")
				.append("*IssueType:* ").append(settings.getByLocator(previousIssue.getIssueType()).getLongName()).append("\n")
				.append("*TicketIds:* ").append(ticketIds.toString()).append("\n");

		return null != previousIssue.getIssueDescription()
				? builder.append("*IssueDescription:* ").append("\n").append(previousIssue.getIssueDescription()).toString()
				: builder.toString();
	}

	/*
	 * Remove all possible previous ${MARKER} fields from description even after
	 * user manual changes
	 */
	private String markerNullifier(String description) {
		// Dirty markers remover
		description = description.replaceAll(MARKER.concat("\n\n"), "");
		description = description.replaceAll(MARKER.concat("\n"), "");
		description = description.replaceAll(MARKER, "");
		return description;
	}

	public void setProcessingIds(LoadingCache<String, String> cache) {
		this.processingIds = cache;
	}

	public LoadingCache<String, String> getProcessIds() {
		return processingIds;
	}
}