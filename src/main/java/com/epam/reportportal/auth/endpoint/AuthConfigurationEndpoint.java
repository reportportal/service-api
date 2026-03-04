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

package com.epam.reportportal.auth.endpoint;

import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.auth.integration.handler.DeleteAuthIntegrationHandler;
import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationHandler;
import com.epam.reportportal.base.infrastructure.model.integration.auth.AbstractAuthResource;
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings/auth")
@Tag(name = "auth-configuration-endpoint", description = "Auth Configuration Endpoint")
public class AuthConfigurationEndpoint {

  private final CreateAuthIntegrationHandler createAuthIntegrationHandler;

  private final DeleteAuthIntegrationHandler deleteAuthIntegrationHandler;

  private final GetAuthIntegrationHandler getAuthIntegrationHandler;

  @Autowired
  public AuthConfigurationEndpoint(CreateAuthIntegrationHandler createAuthIntegrationHandler,
      DeleteAuthIntegrationHandler deleteAuthIntegrationHandler,
      GetAuthIntegrationHandler getAuthIntegrationHandler) {
    this.createAuthIntegrationHandler = createAuthIntegrationHandler;
    this.deleteAuthIntegrationHandler = deleteAuthIntegrationHandler;
    this.getAuthIntegrationHandler = getAuthIntegrationHandler;
  }

  /**
   * Creates or updates auth integration settings.
   *
   * @param request  Update request
   * @param authType Type of Auth
   * @param user     User
   * @return Successful message or an error
   */
  @Transactional
  @PostMapping(value = "/{authType}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Create new auth integration")
  public AbstractAuthResource createAuthIntegration(@RequestBody @Valid UpdateAuthRQ request,
      @AuthenticationPrincipal ReportPortalUser user,
      @PathVariable AuthIntegrationType authType) {
    return createAuthIntegrationHandler.createAuthIntegration(authType, request, user);
  }

  /**
   * Creates or updates auth integration settings
   *
   * @param request       Update request
   * @param authType      Type of Auth
   * @param user          User
   * @param integrationId Integration ID
   * @return Successful message or an error
   */
  @Transactional
  @PutMapping(value = "/{authType}/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update auth integration")
  public AbstractAuthResource updateAuthIntegration(@RequestBody @Valid UpdateAuthRQ request,
      @AuthenticationPrincipal ReportPortalUser user,
      @PathVariable AuthIntegrationType authType, @PathVariable Long integrationId) {
    return createAuthIntegrationHandler.updateAuthIntegration(authType, integrationId, request,
        user);
  }

  /**
   * Get auth settings by type.
   *
   * @param authType Type of Auth
   * @return Successful message or an error
   */
  @Transactional(readOnly = true)
  @GetMapping(value = "/{authType}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Retrieves auth settings")
  public AbstractAuthResource getSettings(@PathVariable AuthIntegrationType authType) {
    return getAuthIntegrationHandler.getIntegrationByType(authType);
  }

  /**
   * Deletes LDAP auth settings.
   *
   * @param integrationId Type of Auth
   * @return Successful message or an error
   */
  @Transactional
  @DeleteMapping(value = "/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Retrieves auth settings")
  public OperationCompletionRS deleteSettings(@PathVariable Long integrationId) {
    return deleteAuthIntegrationHandler.deleteAuthIntegrationById(integrationId);
  }

  @InitBinder
  public void initBinder(final WebDataBinder webdataBinder) {
    webdataBinder.registerCustomEditor(AuthIntegrationType.class, new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) throws IllegalArgumentException {
        setValue(AuthIntegrationType.fromId(text)
            .orElseThrow(
                () -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE, text)));
      }
    });
  }
}
