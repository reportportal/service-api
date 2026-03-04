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

import com.epam.reportportal.auth.integration.github.RPOAuth2User;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates Last Login field in database User entity.
 *
 * @author Andrei Varabyeu
 */
@Component
public class UiAuthenticationSuccessEventHandler {

  private UserRepository userRepository;

  private PersonalProjectService personalProjectService;

  /**
   * Event handler for successful UI authentication events. Updates the last login date for the user and generates a
   * personal project if the user has no projects.
   */
  @Autowired
  public UiAuthenticationSuccessEventHandler(UserRepository userRepository,
      PersonalProjectService personalProjectService) {
    this.userRepository = userRepository;
    this.personalProjectService = personalProjectService;
  }

  /**
   * Handles the UI user signed in event. Updates the last login date for the user and generates a personal project if
   * the user has no projects. Also, if the user is inactive, it will be activated for SAML authentication.
   *
   * @param event the UI user signed in event
   */
  @EventListener
  @Transactional
  public void onApplicationEvent(UiUserSignedInEvent event) {
    String username = event.getAuthentication().getName();

    userRepository.updateLastLoginDate(username);

    /* TODO: Do not generate personal project for now. Waiting for requirements
    if (MapUtils.isEmpty(acquireUser(event.getAuthentication()).getOrganizationDetails())) {
      User user = userRepository.findByLogin(username)
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
      Project project = personalProjectService.generatePersonalProject(user);
      user.getProjects().addAll(project.getUsers());
    }
    */
  }

  private ReportPortalUser acquireUser(Authentication authentication) {
    if (authentication instanceof Saml2Authentication rpAuth) {
      userRepository.findByLogin(rpAuth.getName())
          .filter(user -> !user.getActive())
          .ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
          });
      return userRepository.findByLogin(rpAuth.getName())
          .map(user -> ReportPortalUser.userBuilder().fromUser(user))
          .orElseThrow(() -> new ReportPortalException(
              ErrorType.USER_NOT_FOUND, rpAuth.getPrincipal()
          ));
    } else if (authentication.getPrincipal() instanceof RPOAuth2User ghUser) {
      if (!(ghUser.getReportPortalUser()).isEnabled()) {
        SecurityContextHolder.clearContext();
        throw new LockedException("User account is locked");
      }
      return ghUser.getReportPortalUser();
    } else {
      if (!((ReportPortalUser) authentication.getPrincipal()).isEnabled()) {
        SecurityContextHolder.clearContext();
        throw new LockedException("User account is locked");
      }
      return (ReportPortalUser) authentication.getPrincipal();
    }
  }
}
