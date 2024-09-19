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

package com.epam.ta.reportportal.core.project.impl;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;
import static com.epam.ta.reportportal.ws.converter.converters.ExceptionConverter.TO_ERROR_RS;

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ProjectBulkDeletedEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectDeletedEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectIndexEvent;
import com.epam.ta.reportportal.core.project.DeleteProjectHandler;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.DeleteBulkRS;
import com.epam.ta.reportportal.util.FeatureFlagHandler;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pavel Bortnik
 */
@Service
@Transactional
public class DeleteProjectHandlerImpl implements DeleteProjectHandler {

  private final ProjectRepository projectRepository;

  private final UserRepository userRepository;

  private final LogIndexer logIndexer;

  private final AnalyzerServiceClient analyzerServiceClient;

  private final AnalyzerStatusCache analyzerStatusCache;

  private final MessageBus messageBus;

  private final IssueTypeRepository issueTypeRepository;

  private final ContentRemover<Project> projectContentRemover;

  private final LogRepository logRepository;

  private final AttachmentBinaryDataService attachmentBinaryDataService;

  private final FeatureFlagHandler featureFlagHandler;

  @Autowired
  public DeleteProjectHandlerImpl(ProjectRepository projectRepository,
      UserRepository userRepository, LogIndexer logIndexer,
      AnalyzerServiceClient analyzerServiceClient, AnalyzerStatusCache analyzerStatusCache,
      MessageBus messageBus, AttachmentBinaryDataService attachmentBinaryDataService,
      IssueTypeRepository issueTypeRepository, ContentRemover<Project> projectContentRemover,
      LogRepository logRepository, FeatureFlagHandler featureFlagHandler) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.logIndexer = logIndexer;
    this.analyzerServiceClient = analyzerServiceClient;
    this.analyzerStatusCache = analyzerStatusCache;
    this.messageBus = messageBus;
    this.issueTypeRepository = issueTypeRepository;
    this.projectContentRemover = projectContentRemover;
    this.logRepository = logRepository;
    this.featureFlagHandler = featureFlagHandler;
    this.attachmentBinaryDataService = attachmentBinaryDataService;
  }

  @Override
  public OperationCompletionRS deleteProject(Long projectId, ReportPortalUser user) {
    Project project = getProjectById(projectId);
    OperationCompletionRS operationCompletionRs = deleteProject(project);

    publishSpecialProjectDeletedEvent(user, project);
    return operationCompletionRs;
  }

  private void publishSpecialProjectDeletedEvent(ReportPortalUser user, Project project) {
    if (Objects.nonNull(user)) {
      Long userId = user.getUserId();
      String username = user.getUsername();
      publishProjectDeletedEvent(userId, username, project.getId(), project.getName());
    } else {
      publishProjectDeletedEvent(null, RP_SUBJECT_NAME, project.getId(), "personal_project");
    }
  }

  private void publishProjectDeletedEvent(Long userId, String userLogin, Long projectId,
      String projectName) {
    messageBus.publishActivity(new ProjectDeletedEvent(userId, userLogin, projectId, projectName));
  }

  private Project getProjectById(Long projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
  }

  @Override
  public DeleteBulkRS bulkDeleteProjects(List<Long> ids, ReportPortalUser user) {
    final List<ReportPortalException> exceptions = Lists.newArrayList();
    final Map<Long, String> deletedProjectsMap = Maps.newHashMap();
    ids.forEach(projectId -> {
      try {
        Project project = getProjectById(projectId);
        deleteProject(project);
        deletedProjectsMap.put(projectId, project.getName());
      } catch (ReportPortalException ex) {
        exceptions.add(ex);
      }
    });

    publishProjectBulkDeletedEvent(user, deletedProjectsMap.values());

    return new DeleteBulkRS(List.copyOf(deletedProjectsMap.keySet()), Collections.emptyList(),
        exceptions.stream().map(TO_ERROR_RS).collect(Collectors.toList())
    );
  }

  private void publishProjectBulkDeletedEvent(ReportPortalUser user, Collection<String> names) {
    ProjectBulkDeletedEvent bulkDeletedEvent =
        new ProjectBulkDeletedEvent(user.getUserId(), user.getUsername(), names);
    messageBus.publishActivity(bulkDeletedEvent);
  }

  @Override
  public OperationCompletionRS deleteProjectIndex(String projectName, String username) {
    expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(
        ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "There are no analyzer deployed.");

    Project project = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

    User user = userRepository.findByLogin(username)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));

    expect(AnalyzerUtils.getAnalyzerConfig(project).isIndexingRunning(),
        Predicate.isEqual(false)
    ).verify(ErrorType.FORBIDDEN_OPERATION,
        "Index can not be removed until index generation proceeds."
    );

    Cache<Long, Long> analyzeStatus = analyzerStatusCache.getAnalyzeStatus(AUTO_ANALYZER_KEY)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.ANALYZER_NOT_FOUND, AUTO_ANALYZER_KEY));
    expect(analyzeStatus.asMap().containsValue(project.getId()), Predicate.isEqual(false)).verify(
        ErrorType.FORBIDDEN_OPERATION, "Index can not be removed until index generation proceeds.");

    logIndexer.deleteIndex(project.getId());
    messageBus.publishActivity(
        new ProjectIndexEvent(user.getId(), user.getLogin(), project.getId(), project.getName(),
            false
        ));
    return new OperationCompletionRS(
        "Project index with name = '" + projectName + "' is successfully deleted.");
  }

  private OperationCompletionRS deleteProject(Project project) {
    Set<Long> defaultIssueTypeIds =
        issueTypeRepository.getDefaultIssueTypes().stream().map(IssueType::getId)
            .collect(Collectors.toSet());
    Set<IssueType> issueTypesToRemove =
        project.getProjectIssueTypes().stream().map(ProjectIssueType::getIssueType)
            .filter(issueType -> !defaultIssueTypeIds.contains(issueType.getId()))
            .collect(Collectors.toSet());
    projectContentRemover.remove(project);
    projectRepository.delete(project);
    issueTypeRepository.deleteAll(issueTypesToRemove);
    logIndexer.deleteIndex(project.getId());
    analyzerServiceClient.removeSuggest(project.getId());
    logRepository.deleteByProjectId(project.getId());
    attachmentBinaryDataService.deleteAllByProjectId(project.getId());

    return new OperationCompletionRS(
        "Project with id = '" + project.getId() + "' has been successfully deleted.");
  }
}
