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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.INCORRECT_REQUEST;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.entity.project.ProjectUtils.getConfigParameters;
import static java.util.stream.Collectors.toList;

import com.epam.reportportal.model.project.AnalyzerConfig;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.config.AnalyzerType;
import com.epam.ta.reportportal.core.analyzer.strategy.LaunchAnalysisStrategy;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.UpdateLaunchHandler;
import com.epam.ta.reportportal.core.launch.attribute.LaunchAttributeHandlerService;
import com.epam.ta.reportportal.core.launch.cluster.UniqueErrorAnalysisStarter;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.BulkRQ;
import com.epam.ta.reportportal.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.model.launch.cluster.CreateClustersRQ;
import com.epam.ta.reportportal.util.ItemInfoUtils;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter;
import com.epam.ta.reportportal.ws.reporting.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link UpdateLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateLaunchHandlerImpl implements UpdateLaunchHandler {

  private final GetProjectHandler getProjectHandler;
  private final GetLaunchHandler getLaunchHandler;
  private final LaunchAccessValidator launchAccessValidator;

  private final LaunchRepository launchRepository;

  private final LogIndexer logIndexer;

  private final Map<AnalyzerType, LaunchAnalysisStrategy> launchAnalysisStrategyMapping;

  private final UniqueErrorAnalysisStarter uniqueErrorAnalysisStarter;

  private final LaunchAttributeHandlerService launchAttributeHandlerService;

  @Autowired
  public UpdateLaunchHandlerImpl(GetProjectHandler getProjectHandler,
      GetLaunchHandler getLaunchHandler, LaunchAccessValidator launchAccessValidator,
      LaunchRepository launchRepository, LogIndexer logIndexer,
      Map<AnalyzerType, LaunchAnalysisStrategy> launchAnalysisStrategyMapping,
      @Qualifier("uniqueErrorAnalysisStarterAsync")
      UniqueErrorAnalysisStarter uniqueErrorAnalysisStarter,
      LaunchAttributeHandlerService launchAttributeHandlerService) {
    this.getProjectHandler = getProjectHandler;
    this.getLaunchHandler = getLaunchHandler;
    this.launchAccessValidator = launchAccessValidator;
    this.launchRepository = launchRepository;
    this.launchAnalysisStrategyMapping = launchAnalysisStrategyMapping;
    this.logIndexer = logIndexer;
    this.uniqueErrorAnalysisStarter = uniqueErrorAnalysisStarter;
    this.launchAttributeHandlerService = launchAttributeHandlerService;
  }

  @Override
  public OperationCompletionRS updateLaunch(Long launchId,
      MembershipDetails membershipDetails, ReportPortalUser user, UpdateLaunchRQ rq) {
    Project project = getProjectHandler.get(membershipDetails);
    Launch launch = launchRepository.findById(launchId)
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));
    validate(launch, user, membershipDetails, rq.getMode());

    LaunchModeEnum previousMode = launch.getMode();

    launch = new LaunchBuilder(launch).addMode(rq.getMode()).addDescription(rq.getDescription())
        .overwriteAttributes(rq.getAttributes()).get();
    launchAttributeHandlerService.handleLaunchUpdate(launch, user);
    launchRepository.save(launch);

    if (!previousMode.equals(launch.getMode())) {
      reindexLogs(launch, AnalyzerUtils.getAnalyzerConfig(project), project.getId());
    }
    return new OperationCompletionRS(
        "Launch with ID = '" + launch.getId() + "' successfully updated.");
  }

  @Override
  public List<OperationCompletionRS> updateLaunch(BulkRQ<Long, UpdateLaunchRQ> rq,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    return rq.getEntities().entrySet().stream()
        .map(entry -> updateLaunch(entry.getKey(), membershipDetails, user, entry.getValue()))
        .collect(toList());
  }

  @Override
  public OperationCompletionRS startLaunchAnalyzer(AnalyzeLaunchRQ analyzeRQ,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    AnalyzerType analyzerType = AnalyzerType.fromString(analyzeRQ.getAnalyzerTypeName());
    launchAnalysisStrategyMapping.get(analyzerType).analyze(analyzeRQ, membershipDetails, user);
    return new OperationCompletionRS(
        analyzerType.getName() + " analysis for launch with ID='" + analyzeRQ.getLaunchId()
            + "' started.");
  }

  @Override
  @Transactional
  public OperationCompletionRS createClusters(CreateClustersRQ createClustersRQ,
      MembershipDetails membershipDetails, ReportPortalUser user) {

    final Launch launch = getLaunchHandler.get(createClustersRQ.getLaunchId());
    launchAccessValidator.validate(launch, membershipDetails, user);
    //TODO should be put inside *Validator after validators refactoring
    expect(launch.getStatus(), not(statusIn(StatusEnum.IN_PROGRESS))).verify(INCORRECT_REQUEST,
        "Cannot analyze launch in progress."
    );

    final Project project = getProjectHandler.get(launch.getProjectId());

    final Map<String, String> configParameters =
        getConfigParameters(project.getProjectAttributes());
    configParameters.put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(),
        String.valueOf(createClustersRQ.isRemoveNumbers())
    );
    uniqueErrorAnalysisStarter.start(ClusterEntityContext.of(launch.getId(), launch.getProjectId()),
        configParameters
    );

    return new OperationCompletionRS(
        Suppliers.formattedSupplier("Clusters generation for launch with ID='{}' started.",
            launch.getId()
        ).get());
  }

  @Override
  public OperationCompletionRS bulkInfoUpdate(BulkInfoUpdateRQ bulkUpdateRq,
      MembershipDetails membershipDetails) {
    expect(getProjectHandler.exists(membershipDetails.getProjectId()), Predicate.isEqual(true)).verify(
        PROJECT_NOT_FOUND, membershipDetails.getProjectId());

    List<Launch> launches = launchRepository.findAllById(bulkUpdateRq.getIds());
    launches.forEach(
        it -> ItemInfoUtils.updateDescription(bulkUpdateRq.getDescription(), it.getDescription())
            .ifPresent(it::setDescription));

    bulkUpdateRq.getAttributes().forEach(it -> {
      switch (it.getAction()) {
        case DELETE: {
          launches.forEach(launch -> {
            ItemAttribute toDelete =
                ItemInfoUtils.findAttributeByResource(launch.getAttributes(), it.getFrom());
            launch.getAttributes().remove(toDelete);
          });
          break;
        }
        case UPDATE: {
          launches.forEach(launch -> ItemInfoUtils.updateAttribute(launch.getAttributes(), it));
          break;
        }
        case CREATE: {
          launches.stream()
              .filter(launch -> ItemInfoUtils.containsAttribute(launch.getAttributes(), it.getTo()))
              .forEach(launch -> {
                ItemAttribute itemAttribute =
                    ItemAttributeConverter.FROM_RESOURCE.apply(it.getTo());
                itemAttribute.setLaunch(launch);
                launch.getAttributes().add(itemAttribute);
              });
          break;
        }
      }
    });

    return new OperationCompletionRS("Attributes successfully updated");
  }

  /**
   * If launch mode has changed - reindex items
   *
   * @param launch Update launch
   */
  private void reindexLogs(Launch launch, AnalyzerConfig analyzerConfig, Long projectId) {
    if (LaunchModeEnum.DEBUG.equals(launch.getMode())) {
      logIndexer.indexLaunchesRemove(projectId, Lists.newArrayList(launch.getId()));
    } else {
      logIndexer.indexLaunchLogs(launch, analyzerConfig);
    }
  }

  /**
   * Valide {@link ReportPortalUser} credentials
   *
   * @param launch {@link Launch}
   * @param user   {@link ReportPortalUser}
   * @param mode   {@link Launch#mode}
   */
  //TODO *Validator refactoring
  private void validate(Launch launch, ReportPortalUser user,
      MembershipDetails membershipDetails, Mode mode) {
    if (user.getUserRole() != UserRole.ADMINISTRATOR && !OrganizationRole.MANAGER.equals(membershipDetails.getOrgRole())) {
      expect(launch.getProjectId(), equalTo(membershipDetails.getProjectId()))
          .verify(ACCESS_DENIED);
      if ((membershipDetails.getOrgRole().lowerThan(OrganizationRole.MANAGER) && membershipDetails.getProjectRole().equals(ProjectRole.VIEWER))) {
        expect(user.getUserId(), Predicate.isEqual(launch.getUserId())).verify(ACCESS_DENIED);
      }
    }
  }

}
