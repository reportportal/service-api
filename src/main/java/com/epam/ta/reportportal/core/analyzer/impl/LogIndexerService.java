/*
 * Copyright (C) 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.util.Predicates.LAUNCH_CAN_BE_INDEXED;
import static java.util.stream.Collectors.summarizingLong;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class LogIndexerService implements LogIndexer {
	private static Logger LOGGER = LoggerFactory.getLogger(LogIndexerService.class);

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private AnalyzerServiceClient analyzerServiceClient;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private MailServiceFactory mailServiceFactory;

	@Override
	public void indexLog(Log log) {
		IndexLaunch rq = createRqLaunch(log);
		if (rq != null) {
			analyzerServiceClient.index(Collections.singletonList(rq));
		}
	}

	@Override
	public CompletableFuture<Long> indexLogs(Long launchId, List<TestItem> testItems) {
		CompletableFuture<Long> result = new CompletableFuture<>();
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		if (LAUNCH_CAN_BE_INDEXED.test(launch)) {
			List<IndexTestItem> rqTestItems = prepareItemsForIndexing(testItems);
			if (!CollectionUtils.isEmpty(rqTestItems)) {
				IndexLaunch rqLaunch = new IndexLaunch();
				rqLaunch.setLaunchId(launchId);
				rqLaunch.setLaunchName(launch.getName());
				rqLaunch.setProjectId(launch.getProjectId());
				Project project = projectRepository.findById(launch.getProjectId())
						.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, launch.getProjectId()));
				rqLaunch.setAnalyzerConfig(getAnalyzerConfig(project));
				rqLaunch.setTestItems(rqTestItems);

				result.complete(analyzerServiceClient.index(Collections.singletonList(rqLaunch))
						.stream()
						.collect(summarizingLong(f -> f.exceptionally(ex -> {
							result.completeExceptionally(ex);
							throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR);
						}).join().getItems().size()))
						.getSum());

				/*CompletableFuture.allOf(results.stream().toArray((IntFunction<CompletableFuture<IndexRs>[]>) CompletableFuture[]::new))
						.thenApply(() -> results.stream().flatMap(f -> f.join().getItems().stream().collect(summ)));*/

				return result;

			}
		}
		result.complete(0L);
		return result;
	}

	@Override
	public void deleteIndex(Long project) {
		analyzerServiceClient.deleteIndex(project);
	}

	@Override
	public void cleanIndex(Long index, List<Long> ids) {
		analyzerServiceClient.cleanIndex(index, ids);
	}

	@Override
	public void indexProjectData(Project project, User user) {
		List<Long> launchIds = launchRepository.findLaunchIdsByProjectId(project.getId());
		List<CompletableFuture<Long>> result = new ArrayList<>();
		launchIds.forEach(id -> {
			List<Long> itemIds = testItemRepository.selectIdsNotInIssueByLaunch(id, TestItemIssueGroup.TO_INVESTIGATE.getValue());
			result.add(indexLogs(id, testItemRepository.findAllById(itemIds)));
		});
		long sum = result.stream().mapToLong(f -> {
			try {
				return f.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR);
			}
		}).sum();
		mailServiceFactory.getDefaultEmailService(true).sendIndexFinishedEmail("Index generation has been finished", user.getEmail(), sum);
	}

	/**
	 * Creates {@link IndexLaunch} for specified log if
	 * it is suitable for indexing or else returns <code>null</code>
	 *
	 * @param log Log to be converted
	 * @return Prepared {@link IndexLaunch} rq for indexing
	 */
	private IndexLaunch createRqLaunch(Log log) {
		if (!isLevelSuitable(log)) {
			return null;
		}
		IndexLaunch rqLaunch = null;
		TestItem testItem = testItemRepository.findById(log.getTestItem().getItemId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, log.getTestItem().getItemId()));
		if (ITEM_CAN_BE_INDEXED.test(testItem)) {
			Launch launch = launchRepository.findById(testItem.getLaunch().getId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, testItem.getLaunch().getId()));
			if (LAUNCH_CAN_BE_INDEXED.test(launch)) {
				rqLaunch = new IndexLaunch();
				rqLaunch.setLaunchId(launch.getId());
				rqLaunch.setLaunchName(launch.getName());
				rqLaunch.setProjectId(launch.getProjectId());
				rqLaunch.setTestItems(Collections.singletonList(AnalyzerUtils.fromTestItem(testItem, Collections.singletonList(log))));
			}
		}
		return rqLaunch;
	}

	/**
	 * Checks if the log is suitable for indexing in analyzer.
	 * Log's level is greater or equal than {@link LogLevel#ERROR}
	 *
	 * @param log Log for check
	 * @return true if suitable
	 */
	private boolean isLevelSuitable(Log log) {
		return null != log && null != log.getLogLevel() && log.getLogLevel() >= LogLevel.ERROR.toInt();
	}

	/**
	 * Creates {@link IndexTestItem} from suitable {@link TestItem}
	 * for indexing with logs greater than {@link LogLevel#ERROR}
	 *
	 * @param testItems Test item for preparing
	 * @return Prepared list of {@link IndexTestItem} for indexing
	 */
	private List<IndexTestItem> prepareItemsForIndexing(List<TestItem> testItems) {
		return testItems.stream()
				.filter(ITEM_CAN_BE_INDEXED)
				.map(it -> AnalyzerUtils.fromTestItem(it,
						logRepository.findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(Collections.singletonList(it.getItemId()),
								LogLevel.ERROR.toInt()
						)
				))
				.filter(it -> !CollectionUtils.isEmpty(it.getLogs()))
				.collect(toList());
	}
}
