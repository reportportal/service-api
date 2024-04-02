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

package com.epam.ta.reportportal.core.user.impl;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.core.events.activity.UserDeletedEvent;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.strategy.EmailNotificationStrategy;
import com.epam.ta.reportportal.util.email.strategy.EmailTemplate;
import com.google.common.collect.Lists;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerImplTest {

  @Mock
  private UserRepository repository;

  @Mock
  private UserBinaryDataService dataStore;

  @Mock
  private ContentRemover<User> userContentRemover;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private Map<EmailTemplate, EmailNotificationStrategy> emailNotificationStrategyMapping;

  @Mock
  private EmailNotificationStrategy emailNotificationStrategy;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private DeleteUserHandlerImpl handler;

  @Test
  void deleteUser() {
    User user = new User();
    user.setId(2L);
    user.setLogin("test");

    doReturn(Optional.of(user)).when(repository).findById(2L);
    when(projectRepository.findAllByUserLogin(user.getLogin())).thenReturn(Lists.newArrayList());
    doNothing().when(dataStore).deleteUserPhoto(any());
    when(emailNotificationStrategyMapping.get(any())).thenReturn(emailNotificationStrategy);
    doNothing().when(emailNotificationStrategy).sendEmail(any(), anyMap());
    doNothing().when(applicationEventPublisher).publishEvent(isA(UserDeletedEvent.class));

    handler.deleteUser(
        2L, getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L));

    verify(repository, times(1)).findById(2L);
    verify(dataStore, times(1)).deleteUserPhoto(any());

  }

  @Test
  void deleteNotExistedUser() {
    when(repository.findById(12345L)).thenReturn(Optional.empty());

    final ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> handler.deleteUser(12345L,
            getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L)
        ));
    assertEquals("User '12345' not found.", exception.getMessage());
  }

  @Test
  void deleteOwnAccount() {
    User user = new User();
    user.setId(1L);

    doReturn(Optional.of(user)).when(repository).findById(1L);

    final ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> handler.deleteUser(1L,
            getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L)
        ));
    assertEquals("You do not have enough permissions. You cannot delete own account", exception.getMessage());

    verify(repository, times(1)).findById(1L);
    verify(repository, times(0)).delete(any(User.class));
  }

}