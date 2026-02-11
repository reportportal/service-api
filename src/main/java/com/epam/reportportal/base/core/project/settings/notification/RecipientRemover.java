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

package com.epam.reportportal.base.core.project.settings.notification;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import com.epam.reportportal.base.infrastructure.persistence.dao.SenderCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ivan Budaev
 */
@Service
public class RecipientRemover implements ProjectRecipientHandler {

  private final SenderCaseRepository senderCaseRepository;

  @Autowired
  public RecipientRemover(SenderCaseRepository senderCaseRepository) {
    this.senderCaseRepository = senderCaseRepository;
  }

  /**
   * @param users   {@link User} collection to remove from project recipient list
   * @param project {@link Project}
   */
  @Override
  public void handle(Iterable<User> users, Project project) {
    final Set<String> toExclude = stream(users.spliterator(), false).map(
        user -> asList(user.getEmail().toLowerCase(),
            user.getLogin().toLowerCase()
        )).flatMap(List::stream).collect(toSet());
    /* Current recipients of specified project */
    senderCaseRepository.findAllByProjectId(project.getId()).forEach(senderCase -> {
      // saved - list of saved user emails before changes
      senderCaseRepository.deleteRecipients(senderCase.getId(), toExclude);
    });
  }
}
