/*
 * Copyright 2023 EPAM Systems
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

import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.exception.EnvironmentVariablesNotProvidedException;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminPasswordInitializer implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminPasswordInitializer.class);
  private static final String SUPER_ADMIN_LOGIN = "admin@reportportal.internal";
  private static final String ERROR_MSG = "Password not set in environment variable";
  public static final String USER_LAST_LOGIN = "last_login";
  public static final Integer INITIAL_LAST_LOGIN = 0;

  private final UserRepository userRepository;

  @Value("${rp.initial.admin.password:}")
  private String adminPassword;

  public AdminPasswordInitializer(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    User user = userRepository.findByLogin(SUPER_ADMIN_LOGIN)
        .orElseThrow(() -> new EntityNotFoundException(SUPER_ADMIN_LOGIN + " not found"));
    Object lastLogin = ofNullable(user.getMetadata())
        .flatMap(metadata -> ofNullable(metadata.getMetadata()))
        .map(meta -> meta.get(USER_LAST_LOGIN))
        .orElseGet(() -> Optional.of(INITIAL_LAST_LOGIN));
    checkPasswordEnvVariable(lastLogin);

    boolean isMatches = passwordEncoder().matches(adminPassword, user.getPassword());
    if (!isMatches && lastLogin.equals(INITIAL_LAST_LOGIN) && StringUtils.isNotEmpty(adminPassword)) {
      updatePasswordForDefaultAdmin(user);
    }
  }

  private void updatePasswordForDefaultAdmin(User defaultAdmin) {
    defaultAdmin.setPassword(passwordEncoder().encode(adminPassword));
    userRepository.save(defaultAdmin);
  }

  private void checkPasswordEnvVariable(Object lastLogin) {
    if (StringUtils.isBlank(adminPassword) && lastLogin.equals(INITIAL_LAST_LOGIN)) {
      LOGGER.error(ERROR_MSG);
      throw new EnvironmentVariablesNotProvidedException(ERROR_MSG);
    }
  }

  private PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
