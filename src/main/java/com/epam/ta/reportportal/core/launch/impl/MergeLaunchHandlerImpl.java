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

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_IS_NOT_FINISHED;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.UNSUPPORTED_MERGE_STRATEGY_TYPE;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.LaunchMergeFactory;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.MergeStrategyType;
import com.epam.ta.reportportal.core.launch.MergeLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.LaunchResource;
import com.epam.ta.reportportal.ws.reporting.MergeLaunchesRQ;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel_Bortnik
 */
@Service
public class MergeLaunchHandlerImpl implements MergeLaunchHandler {

  private final LaunchRepository launchRepository;

  private final ProjectRepository projectRepository;

  private final LaunchMergeFactory launchMergeFactory;

  private final LaunchConverter launchConverter;

  private final LogIndexer logIndexer;

  @Autowired
  public MergeLaunchHandlerImpl(LaunchRepository launchRepository,
      ProjectRepository projectRepository,
      LaunchMergeFactory launchMergeFactory, LaunchConverter launchConverter,
      LogIndexer logIndexer) {
    this.launchRepository = launchRepository;
    this.projectRepository = projectRepository;
    this.launchMergeFactory = launchMergeFactory;
    this.launchConverter = launchConverter;
    this.logIndexer = logIndexer;
  }

  @Override
  public LaunchResource mergeLaunches(MembershipDetails membershipDetails,
      ReportPortalUser user, MergeLaunchesRQ rq) {
    Project project = projectRepository.findById(membershipDetails.getProjectId()).orElseThrow(
        () -> new ReportPortalException(PROJECT_NOT_FOUND, membershipDetails.getProjectName()));

    Set<Long> launchesIds = rq.getLaunches();

    expect(CollectionUtils.isNotEmpty(launchesIds), equalTo(true)).verify(
        ErrorType.BAD_REQUEST_ERROR, "At least one launch id should be  specified for merging");

    expect(launchesIds.size() > 0, equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
        "At least 1 launch id should be provided for merging"
    );

    List<Launch> launchesList = launchRepository.findAllById(launchesIds);

    expect(launchesIds.size(), equalTo(launchesList.size())).verify(ErrorType.BAD_REQUEST_ERROR,
        "Not all launches with provided ids were found"
    );

    validateMergingLaunches(launchesList, user, membershipDetails);

    MergeStrategyType type = MergeStrategyType.fromValue(rq.getMergeStrategyType());
    expect(type, notNull()).verify(UNSUPPORTED_MERGE_STRATEGY_TYPE, type);

    Launch newLaunch = launchMergeFactory.getLaunchMergeStrategy(type)
        .mergeLaunches(membershipDetails, user, rq, launchesList);
    newLaunch.setStatus(StatisticsHelper.getStatusFromStatistics(newLaunch.getStatistics()));

    launchRepository.deleteAll(launchesList);

    logIndexer.indexLaunchLogs(newLaunch, AnalyzerUtils.getAnalyzerConfig(project));

    return launchConverter.TO_RESOURCE.apply(newLaunch);
  }

  /**
   * Validations for merge launches request parameters and data
   *
   * @param launches       {@link List} of the {@link Launch}
   * @param user           {@link ReportPortalUser}
   * @param projectDetails {@link ReportPortalUser.ProjectDetails}
   */
  private void validateMergingLaunches(List<Launch> launches, ReportPortalUser user,
      MembershipDetails membershipDetails) {

    /*
     * ADMINISTRATOR and PROJECT_MANAGER+ users have permission to merge not-only-own
     * launches
     */
    boolean isUserValidate = !(user.getUserRole().equals(ADMINISTRATOR)
            || membershipDetails.getOrgRole().sameOrHigherThan(OrganizationRole.MANAGER)
    );

    launches.forEach(launch -> {
      expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launch);

      expect(launch.getStatus(), not(Preconditions.statusIn(IN_PROGRESS))).verify(
          LAUNCH_IS_NOT_FINISHED,
          Suppliers.formattedSupplier("Cannot merge launch '{}' with status '{}'", launch.getId(),
              launch.getStatus()
          )
      );

      expect(launch.getProjectId(), equalTo(membershipDetails.getProjectId())).verify(
          FORBIDDEN_OPERATION, "Impossible to merge launches from different projects.");

      if (isUserValidate) {
        expect(launch.getUserId(), equalTo(user.getUserId())).verify(
            ACCESS_DENIED,
            "You are not an owner of launches or have less than PROJECT_MANAGER project role."
        );
      }
    });
  }

}
