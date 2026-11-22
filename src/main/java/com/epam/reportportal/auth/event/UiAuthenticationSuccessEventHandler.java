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

package com.epam.reportportal.auth.event;

import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates Last Login field in database User entity.
 *
 * @author Andrei Varabyeu
 */
@Component
public class UiAuthenticationSuccessEventHandler {

  private final UserRepository userRepository;

  @Autowired
  public UiAuthenticationSuccessEventHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Handles the UI user signed in event. Updates the last login date for the user. Inactive users are refused earlier,
   * by the authentication itself (JWT converters).
   *
   * @param event the UI user signed in event
   */
  @EventListener
  @Transactional
  public void onApplicationEvent(UiUserSignedInEvent event) {
    userRepository.updateLastLoginDate(event.getAuthentication().getName());
  }
}
