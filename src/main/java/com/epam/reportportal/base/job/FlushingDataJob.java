/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.job;

import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.reportportal.base.core.organization.PersonalOrganizationService;
import com.epam.reportportal.base.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IssueTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectIssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import com.epam.reportportal.base.model.user.CreateUserRQFull;
import com.epam.reportportal.base.ws.converter.builders.UserBuilder;
import java.util.Set;
import java.util.stream.Collectors;
import org.jclouds.blobstore.BlobStore;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class FlushingDataJob implements Job {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
  private static final String SUPERADMIN_PERSONAL = "superadmin_personal";
  private static final String SUPERADMIN = "admin@reportportal.internal";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PersonalProjectService personalProjectService;

  @Autowired
  private AnalyzerServiceClient analyzerServiceClient;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private LogIndexer logIndexer;

  @Autowired
  private IssueTypeRepository issueTypeRepository;

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private UserBinaryDataService dataStore;

  @Autowired
  private BlobStore blobStore;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private FeatureFlagHandler featureFlagHandler;

  @Autowired
  private PersonalOrganizationService personalOrganizationService;

  @Value("${datastore.bucketPrefix}")
  private String bucketPrefix;

  @Value("${datastore.bucketPostfix}")
  private String bucketPostfix;

  @Value("${datastore.defaultBucketName}")
  private String defaultBucketName;

  @Override
  @Transactional(isolation = Isolation.READ_UNCOMMITTED)
  public void execute(JobExecutionContext context) {
    LOGGER.info("Flushing demo instance data is starting...");
    truncateTables();
    projectRepository.findAllProjectNames().stream()
        .filter(it -> !it.equalsIgnoreCase(SUPERADMIN_PERSONAL)).collect(Collectors.toList())
        .forEach(name -> projectRepository.findByName(name).ifPresent(this::deleteProject));
    if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      try {
        blobStore.deleteContainer(defaultBucketName);
      } catch (Exception e) {
        LOGGER.warn("Cannot delete bucket {}", defaultBucketName);
      }
    }
    userRepository.findAll().stream().filter(it -> !it.getLogin().equalsIgnoreCase(SUPERADMIN))
        .forEach(this::deleteUser);
    restartSequences();
    createDefaultUser();
    LOGGER.info("Flushing demo instance data finished");
  }

  /**
   * Get exclusive lock. Kill all running transactions. Truncate tables
   */
  private void truncateTables() {
    jdbcTemplate.execute("""
        BEGIN;
        TRUNCATE TABLE launch RESTART IDENTITY CASCADE;
        TRUNCATE TABLE activity RESTART IDENTITY CASCADE;
        TRUNCATE TABLE owned_entity RESTART IDENTITY CASCADE;
        TRUNCATE TABLE ticket RESTART IDENTITY CASCADE;
        TRUNCATE TABLE issue_ticket RESTART IDENTITY CASCADE;
        DELETE FROM statistics_field WHERE sf_id > 14;
        COMMIT;
        """);
  }

  private void restartSequences() {
    jdbcTemplate.execute("ALTER SEQUENCE project_id_seq RESTART WITH 2");
    jdbcTemplate.execute("ALTER SEQUENCE users_id_seq RESTART WITH 2");
    jdbcTemplate.execute("ALTER SEQUENCE project_attribute_attribute_id_seq RESTART WITH 15");
    jdbcTemplate.execute("ALTER SEQUENCE statistics_field_sf_id_seq RESTART WITH 15");
  }

  private void createDefaultUser() {
    final CreateUserRQFull request = new CreateUserRQFull();
    request.setEmail("default@reportportal.internal");
    request.setFullName("default tester");
    request.setPassword("1q2w3e");
    User user = new UserBuilder().addCreateUserFullRQ(request).addUserRole(UserRole.USER)
        .addPassword(passwordEncoder.encode(request.getPassword())).get();
    projectRepository.save(personalProjectService.generatePersonalProject(user));
    var savedUser = userRepository.save(user);
    personalOrganizationService.createPersonalOrganization(savedUser.getId());
    LOGGER.info("Default user has been successfully created.");
  }

  private void deleteUser(User user) {
    dataStore.deleteUserPhoto(user);
    userRepository.delete(user);
    userRepository.flush();
    LOGGER.info("User with id = '" + user.getId() + "' has been successfully deleted.");
  }

  private void deleteProject(Project project) {
    Set<Long> defaultIssueTypeIds =
        issueTypeRepository.getDefaultIssueTypes().stream().map(IssueType::getId)
            .collect(Collectors.toSet());
    Set<IssueType> issueTypesToRemove =
        project.getProjectIssueTypes().stream().map(ProjectIssueType::getIssueType)
            .filter(issueType -> !defaultIssueTypeIds.contains(issueType.getId()))
            .collect(Collectors.toSet());
    projectRepository.delete(project);
    analyzerServiceClient.removeSuggest(project.getId());
    issueTypeRepository.deleteAll(issueTypesToRemove);
    if (!featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      try {
        blobStore.deleteContainer(bucketPrefix + project.getId() + bucketPostfix);
      } catch (Exception e) {
        LOGGER.warn("Cannot delete attachments bucket " + bucketPrefix + project.getId());
      }
    }
    logIndexer.deleteIndex(project.getId());
    projectRepository.flush();
    attachmentRepository.moveForDeletionByProjectId(project.getId());
    LOGGER.info("Project with id = '" + project.getId() + "' has been successfully deleted.");
  }
}
