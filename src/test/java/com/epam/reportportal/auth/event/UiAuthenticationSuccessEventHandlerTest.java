/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.auth.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class UiAuthenticationSuccessEventHandlerTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final UiAuthenticationSuccessEventHandler handler = new UiAuthenticationSuccessEventHandler(userRepository,
      mock(PersonalProjectService.class));

  private Authentication saml(String login) {
    var auth = mock(Saml2Authentication.class);
    when(auth.getName()).thenReturn(login);
    return auth;
  }

  @Test
  void samlLoginOfDeactivatedUserIsRefusedAndNotReactivated() {
    var user = new User();
    user.setLogin("jdoe");
    user.setActive(false);
    when(userRepository.findByLogin("jdoe")).thenReturn(Optional.of(user));

    assertThrows(LockedException.class,
        () -> ReflectionTestUtils.invokeMethod(handler, "acquireUser", saml("jdoe")));

    assertFalse(user.getActive());
    verify(userRepository, never()).save(any());
  }

  @Test
  void samlLoginOfActiveUserIsAccepted() {
    var user = new User();
    user.setLogin("jdoe");
    user.setActive(true);
    user.setRole(com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole.USER);
    when(userRepository.findByLogin("jdoe")).thenReturn(Optional.of(user));

    ReportPortalUser result = ReflectionTestUtils.invokeMethod(handler, "acquireUser", saml("jdoe"));

    assertEquals("jdoe", result.getUsername());
  }
}
