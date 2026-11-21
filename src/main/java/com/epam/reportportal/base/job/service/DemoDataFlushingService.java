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

package com.epam.reportportal.base.job.service;

import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.reportportal.base.core.logtype.DefaultLogTypeProvider;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.core.organization.PersonalOrganizationService;
import com.epam.reportportal.base.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IssueTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectIssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStore;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import com.epam.reportportal.base.model.user.CreateUserRQFull;
import com.epam.reportportal.base.util.SlugifyUtils;
import com.epam.reportportal.base.ws.converter.builders.UserBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resets a demo instance to its baseline state in a single transaction.
 *
 * <p>Preserves the preset {@value #PRESET_ORGANIZATION} organization with the {@value #SUPERADMIN_PERSONAL} and
 * {@value #DEFAULT_PERSONAL} projects (their test data is cleared), and the personal organization of the recreated
 * default user (without projects). Every other organization, project and user except the superadmin is deleted.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoDataFlushingService {

  private static final String PRESET_ORGANIZATION = "My organization";
  private static final String SUPERADMIN_PERSONAL = "superadmin_personal";
  private static final String DEFAULT_PERSONAL = "default_personal";
  private static final Set<String> RETAINED_PROJECTS = Set.of(SUPERADMIN_PERSONAL, DEFAULT_PERSONAL);
  private static final String SUPERADMIN = "admin@reportportal.internal";
  private static final String DEFAULT_USER = "default@reportportal.internal";
  private static final List<String> SEQUENCES_TO_SYNC = List.of(
      "users_id_seq:users",
      "project_id_seq:project",
      "organization_id_seq:organization",
      "log_type_id_seq:log_type"
  );

  private final JdbcTemplate jdbcTemplate;

  private final PersonalProjectService personalProjectService;

  private final AnalyzerServiceClient analyzerServiceClient;

  private final ProjectRepository projectRepository;

  private final ProjectUserRepository projectUserRepository;

  private final UserRepository userRepository;

  private final LogIndexer logIndexer;

  private final IssueTypeRepository issueTypeRepository;

  private final AttachmentRepository attachmentRepository;

  private final UserBinaryDataService dataStore;

  private final DataStore fileDataStore;

  private final PasswordEncoder passwordEncoder;

  private final FeatureFlagHandler featureFlagHandler;

  private final OrganizationRepository organizationRepository;

  private final OrganizationUserService organizationUserService;

  private final PersonalOrganizationService personalOrganizationService;

  private final DefaultLogTypeProvider defaultLogTypeProvider;

  private final LogTypeRepository logTypeRepository;

  @Value("${datastore.bucketPrefix}")
  private String bucketPrefix;

  @Value("${datastore.defaultBucketName}")
  private String defaultBucketName;

  /**
   * Flushes demo data. Database changes are atomic: if any step fails, the whole reset is rolled back. Storage,
   * analyzer and index cleanup are external calls and are not rolled back.
   */
  @Transactional(isolation = Isolation.READ_UNCOMMITTED)
  public void flush() {
    log.info("Flushing demo instance data is starting...");
    truncateTables();
    Organization presetOrganization = organizationRepository.findByNameIgnoreCase(PRESET_ORGANIZATION)
        .orElseThrow(() -> new IllegalStateException("Organization '" + PRESET_ORGANIZATION + "' is missing"));
    deleteProjects(presetOrganization);
    deleteOrganizations(presetOrganization);
    if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      try {
        fileDataStore.deleteContainer(defaultBucketName);
      } catch (Exception e) {
        log.warn("Cannot delete bucket {}", defaultBucketName);
      }
    }
    userRepository.findAll().stream().filter(it -> !isSuperadmin(it)).forEach(this::deleteUser);
    syncSequences();
    createDefaultUser(presetOrganization);
    log.info("Flushing demo instance data finished");
  }

  /**
   * Truncates demo test data within the surrounding transaction.
   */
  private void truncateTables() {
    jdbcTemplate.execute("""
        TRUNCATE TABLE launch RESTART IDENTITY CASCADE;
        TRUNCATE TABLE activity RESTART IDENTITY CASCADE;
        TRUNCATE TABLE owned_entity RESTART IDENTITY CASCADE;
        TRUNCATE TABLE ticket RESTART IDENTITY CASCADE;
        TRUNCATE TABLE issue_ticket RESTART IDENTITY CASCADE;
        DELETE FROM statistics_field WHERE sf_id > 14;
        DELETE FROM log_type WHERE is_system = false;
        """);
  }

  /**
   * Deletes every project except the retained ones of the preset organization. Projects are deleted one by one
   * (not through the organization cascade) so that their indexes, suggestions and storage are cleaned as well.
   */
  private void deleteProjects(Organization presetOrganization) {
    projectRepository.findAll().stream()
        .filter(project -> !isRetainedProject(project, presetOrganization))
        .forEach(this::deleteProject);
  }

  private boolean isRetainedProject(Project project, Organization presetOrganization) {
    return presetOrganization.getId().equals(project.getOrganizationId())
        && RETAINED_PROJECTS.contains(project.getName().toLowerCase(Locale.ROOT));
  }

  private void deleteOrganizations(Organization presetOrganization) {
    organizationRepository.findAll().stream()
        .filter(organization -> !organization.getId().equals(presetOrganization.getId()))
        .forEach(organization -> {
          organizationRepository.delete(organization);
          organizationRepository.flush();
          log.info("Organization with id = '{}' has been successfully deleted.", organization.getId());
        });
  }

  private boolean isSuperadmin(User user) {
    return SUPERADMIN.equalsIgnoreCase(user.getEmail()) || SUPERADMIN.equalsIgnoreCase(user.getLogin());
  }

  /**
   * Moves sequences of tables with retained rows right after their current maximum id, and resets the sequences of
   * fully cleaned tables.
   */
  private void syncSequences() {
    SEQUENCES_TO_SYNC.forEach(it -> {
      String[] sequenceAndTable = it.split(":");
      jdbcTemplate.execute(String.format("SELECT setval('%s', COALESCE((SELECT MAX(id) FROM %s), 0) + 1, false)",
          sequenceAndTable[0], sequenceAndTable[1]));
    });
    jdbcTemplate.execute("ALTER SEQUENCE project_attribute_attribute_id_seq RESTART WITH 15");
    jdbcTemplate.execute("ALTER SEQUENCE statistics_field_sf_id_seq RESTART WITH 15");
  }

  private void createDefaultUser(Organization presetOrganization) {
    final CreateUserRQFull request = new CreateUserRQFull();
    request.setEmail(DEFAULT_USER);
    request.setFullName("default tester");
    request.setPassword("1q2w3e");
    User user = new UserBuilder().addCreateUserFullRQ(request).addUserRole(UserRole.USER)
        .addPassword(passwordEncoder.encode(request.getPassword())).get();
    var savedUser = userRepository.save(user);
    organizationUserService.saveOrganizationUser(presetOrganization, savedUser, OrganizationRole.MEMBER.name());
    assignDefaultPersonalProject(presetOrganization, savedUser);
    createPersonalOrganization(savedUser);
    log.info("Default user has been successfully created.");
  }

  private void assignDefaultPersonalProject(Organization presetOrganization, User user) {
    projectRepository.findAllByOrganizationId(presetOrganization.getId()).stream()
        .filter(project -> DEFAULT_PERSONAL.equalsIgnoreCase(project.getName()))
        .findFirst()
        .ifPresentOrElse(
            project -> projectUserRepository.save(
                new ProjectUser().withUser(user).withProject(project).withProjectRole(ProjectRole.EDITOR)),
            () -> createDefaultPersonalProject(presetOrganization, user)
        );
  }

  private void createDefaultPersonalProject(Organization presetOrganization, User user) {
    Project project = personalProjectService.generatePersonalProject(user);
    project.setName(DEFAULT_PERSONAL);
    project.setOrganizationId(presetOrganization.getId());
    project.setSlug(SlugifyUtils.slugify(project.getName()));
    project.setKey(presetOrganization.getSlug() + "." + project.getSlug());
    project.setUpdatedAt(Instant.now());
    Project savedProject = projectRepository.save(project);
    logTypeRepository.saveAll(defaultLogTypeProvider.provideDefaultLogTypes(savedProject.getId()));
    log.info("Project '{}' has been recreated.", DEFAULT_PERSONAL);
  }

  /**
   * Creates the personal organization of the default user and keeps it without projects.
   */
  private void createPersonalOrganization(User user) {
    personalOrganizationService.createPersonalOrganization(user.getId())
        .ifPresentOrElse(
            organization -> projectRepository.findAllByOrganizationId(organization.getId())
                .forEach(this::deleteProject),
            () -> log.warn("Personal organization of the default user has not been created.")
        );
  }

  private void deleteUser(User user) {
    dataStore.deleteUserPhoto(user);
    userRepository.delete(user);
    userRepository.flush();
    log.info("User with id = '{}' has been successfully deleted.", user.getId());
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
        fileDataStore.deleteContainer(String.valueOf(project.getId()));
      } catch (Exception e) {
        log.warn("Cannot delete attachments bucket {}{}", bucketPrefix, project.getId());
      }
    }
    logIndexer.deleteIndexAsync(project.getId());
    projectRepository.flush();
    attachmentRepository.moveForDeletionByProjectId(project.getId());
    log.info("Project with id = '{}' has been successfully deleted.", project.getId());
  }
}
