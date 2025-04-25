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

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.user.impl.CreateUserHandlerImpl.BID_TYPE;
import static com.epam.ta.reportportal.core.user.impl.CreateUserHandlerImpl.INTERNAL_BID_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.CreateInvitationLinkEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.user.CreateUserRQ;
import com.epam.ta.reportportal.model.user.CreateUserRQFull;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
@Disabled("To be deleted")
class CreateUserHandlerImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ServerSettingsRepository settingsRepository;

  @Mock
  private GetProjectHandler getProjectHandler;

  @Mock
  private UserCreationBidRepository userCreationBidRepository;

  @Mock
  private GetIntegrationHandler getIntegrationHandler;

  @Mock
  private ThreadPoolTaskExecutor emailExecutorService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private CreateUserHandlerImpl handler;

  @Test
  void createByNotExistedAdmin() {

    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);
    when(userRepository.findRawById(rpUser.getUserId())).thenReturn(Optional.empty());

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUserByAdmin(new CreateUserRQFull(), rpUser, "url")
    );
    assertEquals("User 'admin' not found.", exception.getMessage());
  }

  @Test
  void createByNotAdmin() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);
    User user = new User();
    user.setRole(UserRole.USER);
    when(userRepository.findRawById(1L)).thenReturn(Optional.of(user));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUserByAdmin(new CreateUserRQFull(), rpUser, "url")
    );
    assertEquals(
        "You do not have enough permissions. Only administrator can create new user. Your role is - USER",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminUserAlreadyExists() {
    final ReportPortalUser rpUser =
        getRpUser("new_user@example.com", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER,
            ProjectRole.EDITOR, 1L);
    User creator = new User();
    creator.setRole(UserRole.ADMINISTRATOR);

    doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
    doReturn(Optional.of(new User())).when(userRepository).findByEmail("new_user@example.com");

    final CreateUserRQFull request = new CreateUserRQFull();
    request.setEmail("new_user@example.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUserByAdmin(request, rpUser, "url")
    );
    assertEquals(
        "User with 'email='new_user@example.com'' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithIncorrectEmail() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);
    User creator = new User();
    creator.setRole(UserRole.ADMINISTRATOR);
    doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());

    final CreateUserRQFull request = new CreateUserRQFull();
    request.setEmail("incorrect@email");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUserByAdmin(request, rpUser, "url")
    );
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'email='incorrect@email''",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithExistedEmail() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);
    User creator = new User();
    creator.setRole(UserRole.ADMINISTRATOR);
    doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
    when(userRepository.findByEmail("correct@domain.com")).thenReturn(Optional.of(new User()));

    final CreateUserRQFull request = new CreateUserRQFull();
    request.setEmail("correct@domain.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUserByAdmin(request, rpUser, "url")
    );
    assertEquals(
        "User with 'email='correct@domain.com'' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithExistedEmailUppercase() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);
    User creator = new User();
    creator.setRole(UserRole.ADMINISTRATOR);
    doReturn(Optional.of(creator)).when(userRepository).findRawById(rpUser.getUserId());
    when(userRepository.findByEmail("correct@domain.com")).thenReturn(Optional.of(new User()));

    final CreateUserRQFull request = new CreateUserRQFull();
    request.setEmail("CORRECT@domain.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUserByAdmin(request, rpUser, "url")
    );
    assertEquals(
        "User with 'email='CORRECT@domain.com'' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

  @Test
  @Disabled("To be deleted")
  void createUserBid() {
    final ReportPortalUser rpUser =
        getRpUser("test@mail.com", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER,
            ProjectRole.VIEWER, 1L);
    final String projectName = TEST_PROJECT_KEY;
    final String email = "email@mail.com";
    final ProjectRole role = ProjectRole.VIEWER;

    final Project project = new Project();
    project.setId(1L);
    project.setName(projectName);

    when(getProjectHandler.get(projectName)).thenReturn(project);
    when(userRepository.existsById(rpUser.getUserId())).thenReturn(true);
    when(getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
        IntegrationGroupEnum.NOTIFICATION
    )).thenReturn(Optional.of(new Integration()));
    doNothing().when(emailExecutorService).execute(any());
    doNothing().when(eventPublisher).publishEvent(isA(CreateInvitationLinkEvent.class));

    CreateUserRQ request = new CreateUserRQ();
    request.setDefaultProject(projectName);
    request.setEmail(email);
    request.setRole(role.name());

    //handler.createUserBid(request, rpUser, "emailUrl");

    final ArgumentCaptor<UserCreationBid> bidCaptor =
        ArgumentCaptor.forClass(UserCreationBid.class);
    verify(userCreationBidRepository, times(1)).save(bidCaptor.capture());

    final UserCreationBid bid = bidCaptor.getValue();

    //assertEquals(projectName, bid.getProjectName());
    assertEquals(email, bid.getEmail());
    //assertEquals(role.name(), bid.getRole());
    assertNotNull(bid.getMetadata());

    assertEquals(INTERNAL_BID_TYPE, String.valueOf(bid.getMetadata().getMetadata().get(BID_TYPE)));

  }

  @Test
  @Disabled("To be deleted")
  public void testInviteUserWithSsoEnabledThrowsException() {
/*    // Arrange
    ServerSettings serverSettings = new ServerSettings();
    serverSettings.setValue("true");
    when(settingsRepository.findByKey(SERVER_USERS_SSO)).thenReturn(Optional.of(serverSettings));

    // Act & Assert
    assertThrows(ReportPortalException.class, () -> handler.createUserBid(new CreateUserRQ(),
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L), "test"));*/
  }


}
