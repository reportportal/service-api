package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.merge.MergeTestItemHandler;
import com.epam.ta.reportportal.core.launch.IMergeLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.util.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.ws.converter.LaunchResourceAssembler;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.DeepMergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.database.entity.ProjectRole.LEAD;
import static com.epam.ta.reportportal.database.entity.Status.IN_PROGRESS;
import static com.epam.ta.reportportal.database.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

@Service
public class MergeLaunchHandler implements IMergeLaunchHandler {

    @Autowired
    private TestItemRepository testItemRepository;

    private ProjectRepository projectRepository;

    private LaunchRepository launchRepository;

    private UserRepository userRepository;

    @Autowired
    private MergeTestItemHandler mergeTestItemHandler;

    @Autowired
    private IIssuesAnalyzer analyzerService;

    @Autowired
    private StatisticsFacadeFactory statisticsFacadeFactory;

    @Autowired
    private Provider<LaunchBuilder> launchBuilder;

    @Autowired
    private LaunchMetaInfoRepository launchCounter;

    @Autowired
    private LaunchResourceAssembler launchResourceAssembler;

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Autowired
    public void setLaunchRepository(LaunchRepository launchRepository) {
        this.launchRepository = launchRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public LaunchResource deepMergeLaunches(String projectName, String launchTargetId, String userName, DeepMergeLaunchesRQ mergeLaunchesRQ) {

        expect(mergeLaunchesRQ.getLaunches().contains(launchTargetId), equalTo(false))
                .verify(FORBIDDEN_OPERATION, "Impossible to merge launch the same launch");

        User user = userRepository.findOne(userName);
        Project project = projectRepository.findOne(projectName);
        expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

        Launch launchTarget = launchRepository.findOne(launchTargetId);
        expect(launchTarget, notNull()).verify(LAUNCH_NOT_FOUND, launchTargetId);

        Set<String> launchesIds = mergeLaunchesRQ.getLaunches();
        List<Launch> launchesList = launchRepository.find(launchesIds);
        validateMergingLaunches(launchesList, user, project);

        mergeSameSuits(projectName, launchTarget, launchesIds, userName, mergeLaunchesRQ.getMergeStrategyType());

        updateChildrenOfLaunch(launchTargetId, launchesIds, mergeLaunchesRQ.isExtendSuitesDescription());

        StatisticsFacade statisticsFacade = statisticsFacadeFactory.
                getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy());
        statisticsFacade.recalculateStatistics(launchTarget);

        launchTarget = launchRepository.findOne(launchTarget.getId());

        launchesList.add(launchTarget);
        launchesList.sort(Comparator.comparing(Launch::getStartTime));

        launchTarget.setDescription(mergeLaunchesRQ.getDescription());
        launchTarget.setTags(mergeLaunchesRQ.getTags());
        launchTarget.setStartTime(launchesList.get(0).getStartTime());
        launchTarget.setEndTime(launchesList.get(launchesList.size()-1).getEndTime());
        launchTarget.setStatus(StatisticsHelper.getStatusFromStatistics(launchTarget.getStatistics()));

        launchRepository.save(launchTarget);
        launchRepository.delete(mergeLaunchesRQ.getLaunches());

        return launchResourceAssembler.toResource(launchTarget);
    }

    private void mergeSameSuits(String projectName, Launch launchTarget, Set<String> launchesList, String userName, String strategy) {
        testItemRepository.findItemsWithType(launchTarget.getId(), TestItemType.SUITE).forEach(suit ->
                {
                    List<String> sameNamedSuitsIds = testItemRepository
                            .findIdsWithNameByLaunchesRef(suit.getName(), launchesList)
                            .stream().collect(toList());

                    MergeTestItemRQ mergeTestItemRQ = new MergeTestItemRQ();
                    mergeTestItemRQ.setItems(sameNamedSuitsIds);
                    mergeTestItemRQ.setMergeStrategyType(strategy);
                    mergeTestItemHandler.mergeTestItem(projectName, suit.getId(), mergeTestItemRQ, userName);
                }
        );
    }

    @Override
    public LaunchResource mergeLaunches(String projectName, String userName, MergeLaunchesRQ mergeLaunchesRQ) {
        User user = userRepository.findOne(userName);
        Project project = projectRepository.findOne(projectName);
        expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

        Set<String> launchesIds = mergeLaunchesRQ.getLaunches();
        List<Launch> launchesList = launchRepository.find(launchesIds);

        validateMergingLaunches(launchesList, user, project);

        StartLaunchRQ startRQ = new StartLaunchRQ();
        startRQ.setMode(mergeLaunchesRQ.getMode());
        startRQ.setDescription(mergeLaunchesRQ.getDescription());
        startRQ.setName(mergeLaunchesRQ.getName());
        startRQ.setTags(mergeLaunchesRQ.getTags());

        launchesList.sort(Comparator.comparing(Launch::getStartTime));
        startRQ.setStartTime(launchesList.get(0).getStartTime());

        Launch launch = launchBuilder.get().addStartRQ(startRQ).addProject(projectName).addStatus(IN_PROGRESS).addUser(userName).build();
        launch.setNumber(launchCounter.getLaunchNumber(launch.getName(), projectName));
        launch.setEndTime(launchesList.get(launchesList.size() - 1).getEndTime());

        launchRepository.save(launch);

        updateChildrenOfLaunch(launch.getId(), mergeLaunchesRQ.getLaunches(),
                mergeLaunchesRQ.isExtendSuitesDescription());

        StatisticsFacade statisticsFacade = statisticsFacadeFactory
                .getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy());
        statisticsFacade.recalculateStatistics(launch);

        launch = launchRepository.findOne(launch.getId());
        launch.setStatus(StatisticsHelper.getStatusFromStatistics(launch.getStatistics()));

        launchRepository.save(launch);
        launchRepository.delete(launchesIds);

        return launchResourceAssembler.toResource(launch);
    }

    /**
     * Validations for merge launches request parameters and data
     *
     * @param launches
     */
    private void validateMergingLaunches(List<Launch> launches, User user, Project project) {
        expect(launches.size(), not(equalTo(0))).verify(BAD_REQUEST_ERROR, launches);

		/*
		 * ADMINISTRATOR and LEAD+ users have permission to merge not-only-own
		 * launches
		 */
        boolean isUserValidate = !(user.getRole().equals(ADMINISTRATOR)
                || project.getUsers().get(user.getId()).getProjectRole().getRoleLevel() >= LEAD.getRoleLevel());
        launches.forEach(launch -> {
            expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launch);

            expect(analyzerService.isPossible(launch.getId()), equalTo(true)).verify(FORBIDDEN_OPERATION,
                    "Impossible to merge launch which under AA processing");

            expect(launch.getStatus(), not(Preconditions.statusIn(IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
                    Suppliers.formattedSupplier("Cannot merge launch '{}' with status '{}'", launch.getId(), launch.getStatus()));

            expect(launch.getProjectRef(), equalTo(project.getId())).verify(FORBIDDEN_OPERATION,
                    "Impossible to merge launches from different projects.");

            if (isUserValidate) {
                expect(launch.getUserRef(), equalTo(user.getId())).verify(ACCESS_DENIED,
                        "You are not an owner of launches or have less than LEAD project role.");
            }
        });
    }

    /**
     * Update test-items of specified launches with new LaunchID
     *
     * @param launchId
     */
    private List<TestItem> updateChildrenOfLaunch(String launchId, Set<String> launches, boolean extendDescription) {
        List<TestItem> testItems = launches.stream().map(id -> {
            Launch launch = launchRepository.findOne(id);
            return testItemRepository.findByLaunch(launch).stream().map(item -> {
                item.setLaunchRef(launchId);
                if (item.getType().sameLevel(TestItemType.SUITE)) {
                    // Add launch reference description for top level items
                    Supplier<String> newDescription = Suppliers
                            .formattedSupplier(((null != item.getItemDescription()) ? item.getItemDescription() : "")
                                    + (extendDescription ? "\r\n@launch '{} #{}'" : ""), launch.getName(), launch.getNumber());
                    item.setItemDescription(newDescription.get());
                }
                return item;
            }).collect(toList());
        }).flatMap(List::stream).collect(toList());
        testItemRepository.save(testItems);
        return testItems.stream().filter(item -> item.getType().sameLevel(TestItemType.SUITE)).collect(toList());
    }

}
