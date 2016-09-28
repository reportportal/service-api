package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.getStatusFromStatistics;
import static com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy.TEST_BASED;
import static com.epam.ta.reportportal.database.entity.Status.IN_PROGRESS;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;

import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;

@Service
class DemoItemsService {
	private Random random = new Random();

	private TestItemRepository testItemRepository;
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	@Qualifier("saveLogsTaskExecutor")
	private TaskExecutor taskExecutor;

	@Autowired
	DemoItemsService(TestItemRepository testItemRepository, StatisticsFacadeFactory statisticsFacadeFactory) {
		this.testItemRepository = testItemRepository;
		this.statisticsFacadeFactory = statisticsFacadeFactory;
	}

	void finishTestItem(String testItemId, String status) {
		TestItem testItem = testItemRepository.findOne(testItemId);
		if ("FAILED".equals(status) && !hasChildren(testItem.getType())) {
			testItem.setIssue(new TestItemIssue(issueType(), null));
		}
		testItem.setStatus(Status.fromValue(status).get());
		testItem.setEndTime(new Date());
		testItemRepository.save(testItem);
		TestItemType testItemType = testItem.getType();
		taskExecutor.execute(() -> {
			if (!hasChildren(testItemType)) {
				StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(TEST_BASED);
				statisticsFacade.updateExecutionStatistics(testItem);
				if (null != testItem.getIssue()) {
					statisticsFacade.updateIssueStatistics(testItem);
				}
			}
		});
	}

	TestItem startTestItem(TestItem rootItemId, String launchId, String name, TestItemType testItemType) {
		TestItem testItem = new TestItem();
		testItem.setLaunchRef(launchId);
		testItem.setStartTime(new Date());
		testItem.setName(name);
		testItem.setParent(rootItemId.getId());
		testItem.setHasChilds(hasChildren(testItemType));
		testItem.setStatus(IN_PROGRESS);
		testItem.setType(testItemType);
		testItem.getPath().addAll(rootItemId.getPath());
		testItem.getPath().add(rootItemId.getId());
		return testItemRepository.save(testItem);
	}

	boolean hasChildren(TestItemType testItemType) {
		return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
				|| testItemType == AFTER_METHOD);
	}

	TestItem startRootItem(String rootItemName, String launchId) {
		TestItem testItem = new TestItem();
		testItem.setLaunchRef(launchId);
		testItem.setStartTime(new Date());
		testItem.setName(rootItemName);
		testItem.setHasChilds(true);
		testItem.setStatus(IN_PROGRESS);
		testItem.setType(SUITE);
		return testItemRepository.save(testItem);
	}

	void finishRootItem(String rootItemId) {
		TestItem testItem = testItemRepository.findOne(rootItemId);
		testItem.setEndTime(new Date());
		testItem.setStatus(getStatusFromStatistics(testItem.getStatistics()));
		testItemRepository.save(testItem);
	}

	private String issueType() {
		final int value = random.nextInt(100);
		if (value < 25) {
			return "PB001";
		} else if (value < 50) {
			return "AB001";
		} else if (value < 75) {
			return "SI001";
		} else {
			return "TI001";
		}
	}

}
