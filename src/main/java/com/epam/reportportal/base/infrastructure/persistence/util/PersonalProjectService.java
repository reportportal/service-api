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

package com.epam.reportportal.base.infrastructure.persistence.util;

import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectUtils.defaultIssueTypes;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectUtils.defaultProjectAttributes;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.epam.reportportal.base.infrastructure.persistence.dao.AttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IssueTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Generates Personal project for provided user
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public final class PersonalProjectService {

  public static final String PERSONAL_PROJECT_POSTFIX = "_personal";
  private final ProjectRepository projectRepository;
  private final AttributeRepository attributeRepository;
  private final IssueTypeRepository issueTypeRepository;

  @Autowired
  public PersonalProjectService(ProjectRepository projectRepository,
      AttributeRepository attributeRepository,
      IssueTypeRepository issueTypeRepository) {
    this.projectRepository = projectRepository;
    this.attributeRepository = attributeRepository;
    this.issueTypeRepository = issueTypeRepository;
  }

  /**
   * Prefix from username with replaced dots as underscores
   *
   * @param username Name of user
   * @return Corresponding personal project name
   */
  @VisibleForTesting
  String generatePersonalProjectName(String username) {
    String initialName = getProjectPrefix(username);

    String name = initialName;
    //iterate until we find free project name
    for (int i = 1; projectRepository.existsByName(name); i++) {
      name = initialName + "_" + i;
    }

    return name;
  }

  /**
   * Generates personal project for provided user
   *
   * @param user User project should be created for
   * @return Built Project object
   */
  public Project generatePersonalProject(User user) {
    Project project = new Project();
    project.setName(generatePersonalProjectName(user.getLogin()));
    project.setCreationDate(Instant.now());

    ProjectUser projectUser = new ProjectUser()
        .withUser(user)
        .withProjectRole(ProjectRole.EDITOR)
        .withProject(project);
    project.setUsers(Sets.newHashSet(projectUser));

    project.setMetadata(new Metadata(Collections.singletonMap("additional_info",
        "Personal project of " + (isNullOrEmpty(user.getFullName()) ? user.getLogin()
            : user.getFullName())
    )));

    var attrs = Arrays.stream(ProjectAttributeEnum.values())
        .map(ProjectAttributeEnum::getAttribute)
        .collect(Collectors.toSet());
    project.setProjectAttributes(
        defaultProjectAttributes(project, attributeRepository.findAllByNameIn(attrs)));

    var locators = Arrays.stream(TestItemIssueGroup.values())
        .map(TestItemIssueGroup::getLocator)
        .toList();
    project.setProjectIssueTypes(
        defaultIssueTypes(project, issueTypeRepository.getDefaultIssueTypes(locators)));

    return project;
  }

  /**
   * Generates prefix for personal project
   *
   * @param username Name of user
   * @return Prefix
   */
  public String getProjectPrefix(String username) {
    String projectName = username.replaceAll("[.@\\-+_%!#$&'*/=?^`{|}~]", "_");
    return (projectName + PERSONAL_PROJECT_POSTFIX).toLowerCase();
  }
}
