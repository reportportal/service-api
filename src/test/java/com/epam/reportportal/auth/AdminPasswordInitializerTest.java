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

package com.epam.reportportal.auth;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.auth.exception.EnvironmentVariablesNotProvidedException;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminPasswordInitializerTest {

  private static final String ADMIN_LOGIN = "admin@reportportal.internal";

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AdminPasswordInitializer initializer;

  @Test
  void adminWithoutLastLoginGetsPasswordFromEnvironment() {
    User admin = admin(new Metadata(new HashMap<>()), "old-password");
    given(userRepository.findByLogin(ADMIN_LOGIN)).willReturn(Optional.of(admin));
    ReflectionTestUtils.setField(initializer, "adminPassword", "new-password");

    initializer.run();

    verify(userRepository).save(admin);
    assertTrue(new BCryptPasswordEncoder().matches("new-password", admin.getPassword()));
  }

  @Test
  void adminWithoutMetadataGetsPasswordFromEnvironment() {
    User admin = admin(null, "old-password");
    given(userRepository.findByLogin(ADMIN_LOGIN)).willReturn(Optional.of(admin));
    ReflectionTestUtils.setField(initializer, "adminPassword", "new-password");

    initializer.run();

    verify(userRepository).save(admin);
  }

  @Test
  void adminWithoutLastLoginAndWithoutPasswordVariableFailsStartup() {
    User admin = admin(new Metadata(new HashMap<>()), "old-password");
    given(userRepository.findByLogin(ADMIN_LOGIN)).willReturn(Optional.of(admin));
    ReflectionTestUtils.setField(initializer, "adminPassword", "");

    assertThrows(EnvironmentVariablesNotProvidedException.class, () -> initializer.run());
  }

  @Test
  void adminWhoHasLoggedInKeepsPassword() {
    User admin = admin(new Metadata(new HashMap<>(Map.of("last_login", 1700000000000L))),
        "old-password");
    given(userRepository.findByLogin(ADMIN_LOGIN)).willReturn(Optional.of(admin));
    ReflectionTestUtils.setField(initializer, "adminPassword", "new-password");

    initializer.run();

    verify(userRepository, never()).save(any());
  }

  private static User admin(Metadata metadata, String rawPassword) {
    User user = new User();
    user.setLogin(ADMIN_LOGIN);
    user.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
    user.setMetadata(metadata);
    return user;
  }
}
