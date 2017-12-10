package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.launch.IRetriesLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.history.status.RetryObject;
import com.epam.ta.reportportal.database.entity.item.RetryType;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

@Service
public class RetriesLaunchHandler implements IRetriesLaunchHandler {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Override
	public void collectRetries(Launch launch) {
		if (isTrue(launch.getHasRetries())) {
			Project project = projectRepository.findOne(launch.getProjectRef());
			StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration()
					.getStatisticsCalculationStrategy());

			List<RetryObject> retries = testItemRepository.findRetries(launch.getId());

			retries.forEach(retry -> {
				List<TestItem> rtr = retry.getRetries();
				TestItem retryRoot = rtr.stream().filter(it -> it.getRetryType().equals(RetryType.ROOT)).findFirst().orElse(null);
				BusinessRule.expect(retryRoot, Predicates.notNull()).verify(ErrorType.FORBIDDEN_OPERATION);

				statisticsFacade.resetExecutionStatistics(retryRoot);
				if (retryRoot.getIssue() != null) {
					statisticsFacade.resetIssueStatistics(retryRoot);
				}

				TestItem lastRetry = rtr.get(rtr.size() - 1);
				rtr.remove(rtr.size() - 1);
				lastRetry.setRetryType(RetryType.LAST);
				lastRetry.setRetries(rtr);
				lastRetry.setParent(retryRoot.getParent());
				testItemRepository.delete(rtr);

				if (!lastRetry.getStatus().equals(Status.PASSED)) {
					lastRetry.setIssue(retryRoot.getIssue());
				}
				statisticsFacade.updateExecutionStatistics(lastRetry);
				if (lastRetry.getIssue() != null) {
					statisticsFacade.updateIssueStatistics(lastRetry);
				}
				testItemRepository.save(lastRetry);
			});
		}
	}
}
