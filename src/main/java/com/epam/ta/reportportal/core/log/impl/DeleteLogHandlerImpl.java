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

package com.epam.ta.reportportal.core.log.impl;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_IS_NOT_FINISHED;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.TEST_ITEM_IS_NOT_FINISHED;
import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.DeleteLogHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

/**
 * Delete Logs handler. Basic implementation of
 * {@link com.epam.ta.reportportal.core.log.DeleteLogHandler} interface.
 *
 * @author Henadzi_Vrubleuski
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteLogHandlerImpl implements DeleteLogHandler {

  private final LogRepository logRepository;

  private final AttachmentRepository attachmentRepository;

  private final ProjectRepository projectRepository;

  private final TestItemService testItemService;

  private final LogIndexer logIndexer;

  private final LogService logService;

  public DeleteLogHandlerImpl(LogRepository logRepository, ProjectRepository projectRepository,
      TestItemService testItemService,
      LogIndexer logIndexer, AttachmentRepository attachmentRepository, LogService logService) {
    this.logRepository = logRepository;
    this.projectRepository = projectRepository;
    this.testItemService = testItemService;
    this.logIndexer = logIndexer;
    this.attachmentRepository = attachmentRepository;
    this.logService = logService;
  }

  @Override
  public OperationCompletionRS deleteLog(Long logId, MembershipDetails membershipDetails,
      ReportPortalUser user) {
    BusinessRule.expect(projectRepository.existsById(membershipDetails.getProjectId()),
            BooleanUtils::isTrue)
        .verify(PROJECT_NOT_FOUND, membershipDetails.getProjectId());

    Log log = validate(logId, user, membershipDetails);
    try {
      logService.deleteLogMessage(membershipDetails.getProjectId(), log.getId());
      logRepository.delete(log);
      ofNullable(log.getAttachment()).ifPresent(
          attachment -> attachmentRepository.moveForDeletion(attachment.getId()));
    } catch (Exception exc) {
      throw new ReportPortalException("Error while Log instance deleting.", exc);
    }

    logIndexer.cleanIndex(membershipDetails.getProjectId(), Collections.singletonList(logId));
    return new OperationCompletionRS(
        formattedSupplier("Log with ID = '{}' successfully deleted.", logId).toString());
  }

  /**
   * Validate specified log against parent objects and project
   *
   * @param logId          - validated log ID value
   * @param membershipDetails  Membership details
   * @return Log
   */
  private Log validate(Long logId, ReportPortalUser user,
      MembershipDetails membershipDetails) {

    Log log = logRepository.findById(logId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LOG_NOT_FOUND, logId));

    Optional<TestItem> itemOptional = ofNullable(log.getTestItem());
    Launch launch = ofNullable(log.getTestItem()).map(testItemService::getEffectiveLaunch)
        .orElseGet(log::getLaunch);

    //TODO check if statistics is right in item results
    if (itemOptional.isPresent()) {
      expect(itemOptional.get().getItemResults().getStatistics(), notNull()).verify(
          TEST_ITEM_IS_NOT_FINISHED, formattedSupplier(
              "Unable to delete log '{}' when test item '{}' in progress state",
              log.getId(),
              itemOptional.get().getItemId()
          ));
    } else {
      expect(launch.getStatus(), not(statusIn(StatusEnum.IN_PROGRESS))).verify(
          LAUNCH_IS_NOT_FINISHED,
          formattedSupplier("Unable to delete log '{}' when launch '{}' in progress state",
              log.getId(), launch.getId())
      );
    }

    expect(launch.getProjectId(), equalTo(membershipDetails.getProjectId())).verify(
        FORBIDDEN_OPERATION,
        formattedSupplier("Log '{}' not under specified '{}' project", logId,
            membershipDetails.getProjectId())
    );

    if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(user.getUserId(),
        launch.getUserId())) {
      /*
       * Only PROJECT_MANAGER roles could delete logs
       */
      expect(membershipDetails.getOrgRole().lowerThan(OrganizationRole.MANAGER)
              && membershipDetails.getProjectRole().equals(ProjectRole.VIEWER), equalTo(false))
          .verify(ACCESS_DENIED);
    }

    return log;
  }
}
