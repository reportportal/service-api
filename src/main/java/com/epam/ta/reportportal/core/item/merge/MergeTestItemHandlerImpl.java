package com.epam.ta.reportportal.core.item.merge;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.core.item.merge.strategy.MergeStrategy;
import com.epam.ta.reportportal.core.item.merge.strategy.MergeStrategyFactory;
import com.epam.ta.reportportal.core.item.merge.strategy.MergeStrategyType;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.item.merge.strategy.MergeUtils.isTestItemStatusIsZeroLevel;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

@Service
public class MergeTestItemHandlerImpl implements MergeTestItemHandler {

    @Autowired
    private TestItemRepository testItemRepository;

    @Autowired
    private StatisticsFacadeFactory statisticsFacadeFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private MergeStrategyFactory mergeStrategyFactory;

    @Override
    public OperationCompletionRS mergeTestItem(String projectName, String item, MergeTestItemRQ rq, String userName) {
        TestItem testItemTarget = validateTestItem(item);
        validateTestItemIsSuite(testItemTarget);
        Launch launchTarget = validateLaunch(testItemTarget.getLaunchRef());
        Project project = validateProject(launchTarget.getProjectRef());
        validateLaunchInProject(launchTarget, project);
        validateUserInProject(project, userName);

        List<TestItem> itemsToMerge = new ArrayList<>();
        Set<String> sourceLaunches = new HashSet<>();
        for (String id : rq.getItems()) {
            TestItem itemToMerge = validateTestItem(id);
            sourceLaunches.add(itemToMerge.getLaunchRef());
            validateTestItemIsSuite(itemToMerge);
            validateTestItemInProject(itemToMerge, project);
            itemsToMerge.add(itemToMerge);
        }
        MergeStrategyType mergeStrategyType = MergeStrategyType.fromValue(rq.getMergeStrategyType());
        expect(mergeStrategyType, Predicates.notNull()).verify(ErrorType.UNSUPPORTED_MERGE_STRATEGY_TYPE, rq.getMergeStrategyType());

        MergeStrategy mergeStrategy = mergeStrategyFactory.getStrategy(mergeStrategyType);
        mergeStrategy.mergeTestItems(testItemTarget, itemsToMerge);

        for (String launchID : sourceLaunches) {
            Launch launch = launchRepository.findOne(launchID);
            recalculateStatistics(launch, project);
        }

        recalculateStatistics(launchTarget, project);

        return new OperationCompletionRS("TestItem with ID = '" + item + "' successfully merged.");
    }

    private void recalculateStatistics(Launch launch, Project project) {
        deleteLaunchStatistics(launch);
        StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy());
        testItemRepository.findByHasChildStatus(false,
                launch.getId()).forEach((item) -> recalculateTestItemStatistics(item, statisticsFacade));

        List<TestItem> withIssues = testItemRepository.findTestItemWithIssues(launch.getId());
        withIssues.forEach(statisticsFacade::updateIssueStatistics);
    }

    private void recalculateTestItemStatistics(TestItem item, StatisticsFacade statisticsFacade) {
        statisticsFacade.updateExecutionStatistics(item);
    }

    private void deleteLaunchStatistics(Launch launch) {
        testItemRepository.findByLaunch(launch).forEach(this::deleteTestItemStatistics);
        launch.getStatistics().setExecutionCounter(new ExecutionCounter(0, 0, 0, 0));
        launch.getStatistics().setIssueCounter(new IssueCounter());
        launchRepository.save(launch);
    }

    private void deleteTestItemStatistics(TestItem item) {
        item.getStatistics().setExecutionCounter(new ExecutionCounter(0, 0, 0, 0));
        item.getStatistics().setIssueCounter(new IssueCounter());
        testItemRepository.save(item);
    }

    private void validateLaunchInProject(Launch launch, Project project) {
        expect(launch.getProjectRef(), equalTo(project.getId())).verify(ACCESS_DENIED);
    }

    private void validateTestItemInProject(TestItem testItem, Project project) {
        Launch launch = launchRepository.findOne(testItem.getLaunchRef());
        expect(launch.getProjectRef(), equalTo(project.getId())).verify(ACCESS_DENIED);
    }

    private TestItem validateTestItem(String testItemId) {
        TestItem testItem = testItemRepository.findOne(testItemId);
        expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, testItemId);
        return testItem;
    }

    private Launch validateLaunch(String launchId) {
        Launch launch = launchRepository.findOne(launchId);
        expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);
        return launch;
    }

    private Project validateProject(String projectId) {
        Project project = projectRepository.findOne(projectId);
        expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectId);
        return project;
    }

    private User validateUserInProject(Project project, String userName) {
        User user = userRepository.findOne(userName);
        if (user.getRole() != UserRole.ADMINISTRATOR) {
            expect(true, equalTo(project.getUsers().containsKey(userName))).verify(ACCESS_DENIED);
        }
        return user;
    }

    private void validateTestItemIsSuite(TestItem testItem) {
        expect(true, equalTo(isTestItemStatusIsZeroLevel(testItem))).verify(ErrorType.INCORRECT_REQUEST, testItem.getId());
    }
}
