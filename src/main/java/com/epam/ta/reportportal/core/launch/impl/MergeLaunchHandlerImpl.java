/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.LaunchMergeFactory;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.MergeStrategyType;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel_Bortnik
 */
@Service
public class MergeLaunchHandlerImpl implements com.epam.ta.reportportal.core.launch.MergeLaunchHandler {

	private final LaunchRepository launchRepository;

	private final ProjectRepository projectRepository;

	private final LaunchMergeFactory launchMergeFactory;

	private final LaunchConverter launchConverter;

	private final LogIndexer logIndexer;

	@Autowired
	public MergeLaunchHandlerImpl(LaunchRepository launchRepository, ProjectRepository projectRepository,
			LaunchMergeFactory launchMergeFactory, LaunchConverter launchConverter, LogIndexer logIndexer) {
		this.launchRepository = launchRepository;
		this.projectRepository = projectRepository;
		this.launchMergeFactory = launchMergeFactory;
		this.launchConverter = launchConverter;
		this.logIndexer = logIndexer;
	}

	@Override
	public LaunchResource mergeLaunches(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, MergeLaunchesRQ rq) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectName()));

		Set<Long> launchesIds = rq.getLaunches();

		expect(CollectionUtils.isNotEmpty(launchesIds), equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"At least one launch id should be  specified for merging"
		);

		expect(launchesIds.size() > 0, equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"At least 1 launch id should be provided for merging"
		);

		List<Launch> launchesList = launchRepository.findAllById(launchesIds);

		expect(launchesIds.size(), equalTo(launchesList.size())).verify(ErrorType.BAD_REQUEST_ERROR,
				"Not all launches with provided ids were found"
		);

		validateMergingLaunches(launchesList, user, projectDetails);

		MergeStrategyType type = MergeStrategyType.fromValue(rq.getMergeStrategyType());
		expect(type, notNull()).verify(UNSUPPORTED_MERGE_STRATEGY_TYPE, type);

		Launch newLaunch = launchMergeFactory.getLaunchMergeStrategy(type).mergeLaunches(projectDetails, user, rq, launchesList);

		newLaunch.setStatus(StatisticsHelper.getStatusFromStatistics(newLaunch.getStatistics()));

		launchRepository.save(newLaunch);
		logIndexer.cleanIndex(project.getId(), new ArrayList<>(launchesIds));
		launchRepository.deleteAll(launchesList);
		logIndexer.indexLaunchLogs(project.getId(), newLaunch.getId(), AnalyzerUtils.getAnalyzerConfig(project));

		return launchConverter.TO_RESOURCE.apply(newLaunch);
	}

	/**
	 * Validations for merge launches request parameters and data
	 *
	 * @param launches       {@link List} of the {@link Launch}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link ReportPortalUser.ProjectDetails}
	 */
	private void validateMergingLaunches(List<Launch> launches, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {

		/*
		 * ADMINISTRATOR and PROJECT_MANAGER+ users have permission to merge not-only-own
		 * launches
		 */
		boolean isUserValidate = !(user.getUserRole().equals(ADMINISTRATOR) || projectDetails.getProjectRole()
				.sameOrHigherThan(ProjectRole.PROJECT_MANAGER));

		launches.forEach(launch -> {
			expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launch);

			expect(launch.getStatus(), not(Preconditions.statusIn(IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
					Suppliers.formattedSupplier("Cannot merge launch '{}' with status '{}'", launch.getId(), launch.getStatus())
			);

			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
					"Impossible to merge launches from different projects."
			);

			if (isUserValidate) {
				expect(launch.getUser().getLogin(), equalTo(user.getUsername())).verify(ACCESS_DENIED,
						"You are not an owner of launches or have less than PROJECT_MANAGER project role."
				);
			}
		});
	}

}
