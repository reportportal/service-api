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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.core.item.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.core.launch.IMergeLaunchHandler;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel_Bortnik
 */
@Service
public class MergeLaunchHandler implements IMergeLaunchHandler {

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	//
	//	@Autowired
	//	private MergeStrategyFactory mergeStrategyFactory;
	//
	//	@Autowired
	//	private StatisticsFacadeFactory statisticsFacadeFactory;


	@Autowired
	private TestItemUniqueIdGenerator identifierGenerator;
	//
	//	@Autowired
	//	private ILogIndexer logIndexer;


	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}


	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public LaunchResource mergeLaunches(String projectName, String userName, MergeLaunchesRQ rq) {
		//		User user = userRepository.findOne(userName);
		//		Project project = projectRepository.findOne(projectName);
		//		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		//
		//		Set<String> launchesIds = rq.getLaunches();
		//		expect(launchesIds.size() > 1, equalTo(true)).verify(BAD_REQUEST_ERROR, rq.getLaunches());
		//		List<Launch> launchesList = launchRepository.find(launchesIds);
		//		boolean hasRetries = launchesList.stream().anyMatch(it -> it.getHasRetries() != null);
		//		validateMergingLaunches(launchesList, user, project);
		//
		//		Launch launch = createResultedLaunch(projectName, userName, rq, hasRetries);
		//		boolean isNameChanged = !launch.getName().equals(launchesList.get(0).getName());
		//		updateChildrenOfLaunches(launch.getId(), rq.getLaunches(), rq.isExtendSuitesDescription(), isNameChanged);
		//
		//		MergeStrategyType type = MergeStrategyType.fromValue(rq.getMergeStrategyType());
		//		expect(type, notNull()).verify(UNSUPPORTED_MERGE_STRATEGY_TYPE, type);
		//
		//		// deep merge strategies
		//		if (!type.equals(MergeStrategyType.BASIC)) {
		//			MergeStrategy strategy = mergeStrategyFactory.getStrategy(type);
		//			//  group items by unique id
		//			testItemRepository.findWithoutParentByLaunchRef(launch.getId())
		//					.stream()
		//					.collect(groupingBy(TestItem::getUniqueId))
		//					.entrySet()
		//					.stream()
		//					.map(Map.Entry::getValue)
		//					.filter(items -> items.size() > 1)
		//					.forEach(items -> strategy.mergeTestItems(items.get(0), items.subList(1, items.size())));
		//		}
		//
		//		StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(
		//				project.getConfiguration().getStatisticsCalculationStrategy());
		//		statisticsFacade.recalculateStatistics(launch);
		//
		//		launch = launchRepository.findOne(launch.getId());
		//		launch.setStatus(StatisticsHelper.getStatusFromStatistics(launch.getStatistics()));
		//		launch.setEndTime(rq.getEndTime());
		//
		//		launchRepository.save(launch);
		//		launchRepository.delete(launchesIds);
		//
		//		logIndexer.indexLogs(launch.getId(), testItemRepository.findItemsNotInIssueType(TO_INVESTIGATE.getLocator(), launch.getId()));
		//
		//		return LaunchConverter.TO_RESOURCE.apply(launch);
		return null;
	}

	//	/**
	//	 * Validations for merge launches request parameters and data
	//	 *
	//	 * @param launches
	//	 */
	//	private void validateMergingLaunches(List<Launch> launches, User user, Project project) {
	//		expect(launches.size(), not(equalTo(0))).verify(BAD_REQUEST_ERROR, launches);
	//
	//		/*
	//		 * ADMINISTRATOR and PROJECT_MANAGER+ users have permission to merge not-only-own
	//		 * launches
	//		 */
	//		boolean isUserValidate = !(user.getRole().equals(ADMINISTRATOR) || findUserConfigByLogin(project, user.getId()).getProjectRole()
	//				.sameOrHigherThan(ProjectRole.PROJECT_MANAGER));
	//		launches.forEach(launch -> {
	//			expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launch);
	//
	//			expect(launch.getStatus(), not(Preconditions.statusIn(IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
	//					Suppliers.formattedSupplier("Cannot merge launch '{}' with status '{}'", launch.getId(), launch.getStatus())
	//			);
	//
	//			expect(launch.getProjectRef(), equalTo(project.getId())).verify(FORBIDDEN_OPERATION,
	//					"Impossible to merge launches from different projects."
	//			);
	//
	//			if (isUserValidate) {
	//				expect(launch.getUserRef(), equalTo(user.getId())).verify(ACCESS_DENIED,
	//						"You are not an owner of launches or have less than PROJECT_MANAGER project role."
	//				);
	//			}
	//		});
	//	}
	//
	//	/**
	//	 * Update test-items of specified launches with new LaunchID
	//	 */
	//	private void updateChildrenOfLaunches(String launchId, Set<String> launches, boolean extendDescription, boolean isNameChanged) {
	//		List<TestItem> testItems = launches.stream().flatMap(id -> {
	//			Launch launch = launchRepository.findOne(id);
	//			return testItemRepository.findByLaunch(launch).stream().map(item -> {
	//				item.setLaunchRef(launchId);
	//				if (isNameChanged && identifierGenerator.validate(item.getUniqueId())) {
	//					item.setUniqueId(identifierGenerator.generate(item));
	//				}
	//				if (item.getType().sameLevel(TestItemType.SUITE)) {
	//					// Add launch reference description for top level items
	//					Supplier<String> newDescription = Suppliers.formattedSupplier(
	//							((null != item.getItemDescription()) ? item.getItemDescription() : "") + (extendDescription ?
	//									"\r\n@launch '{} #{}'" :
	//									""), launch.getName(), launch.getNumber());
	//					item.setItemDescription(newDescription.get());
	//				}
	//				return item;
	//			});
	//		}).collect(toList());
	//		testItemRepository.save(testItems);
	//	}
	//
	//	/**
	//	 * Create launch that will be the result of merge
	//	 *
	//	 * @param projectName
	//	 * @param userName
	//	 * @param mergeLaunchesRQ
	//	 * @return launch
	//	 */
	//	private Launch createResultedLaunch(String projectName, String userName, MergeLaunchesRQ mergeLaunchesRQ, boolean hasRetries) {
	//		StartLaunchRQ startRQ = new StartLaunchRQ();
	//		startRQ.setMode(mergeLaunchesRQ.getMode());
	//		startRQ.setDescription(mergeLaunchesRQ.getDescription());
	//		startRQ.setName(mergeLaunchesRQ.getName());
	//		startRQ.setTags(mergeLaunchesRQ.getTags());
	//		startRQ.setStartTime(mergeLaunchesRQ.getStartTime());
	//		Launch launch = new LaunchBuilder().addStartRQ(startRQ).addProject(projectName).addStatus(IN_PROGRESS).addUser(userName).get();
	//		launch.setNumber(launchCounter.getLaunchNumber(launch.getName(), projectName));
	//		launch.setHasRetries(hasRetries ? true : null);
	//		return launchRepository.save(launch);
	//	}
}
