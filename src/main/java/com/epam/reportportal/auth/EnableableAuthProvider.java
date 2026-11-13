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

package com.epam.reportportal.auth;

import com.epam.reportportal.auth.config.password.ClientToken;
import com.epam.reportportal.auth.config.utils.ConvertToOauthToken;
import com.epam.reportportal.auth.event.UiUserSignedInEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Dynamic (enableable) auth provider.
 *
 * @author Andrei Varabyeu
 */
@Component
public abstract class EnableableAuthProvider implements AuthenticationProvider {

  protected final IntegrationRepository integrationRepository;
  protected final ApplicationEventPublisher eventPublisher;
  protected final TokenServicesFacade tokenService;

  public EnableableAuthProvider(IntegrationRepository integrationRepository,
      ApplicationEventPublisher eventPublisher, TokenServicesFacade tokenService) {
    this.integrationRepository = integrationRepository;
    this.eventPublisher = eventPublisher;
    this.tokenService = tokenService;
  }

  protected abstract boolean isEnabled();

  protected abstract AuthenticationProvider getDelegate();

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (isEnabled()) {
      Authentication auth = getDelegate().authenticate(authentication);
      eventPublisher.publishEvent(new UiUserSignedInEvent(auth));
      ConvertToOauthToken convertToOauthToken = new ConvertToOauthToken(tokenService);

      ClientToken clientToken = (ClientToken) authentication;
      return convertToOauthToken.convert(clientToken, auth);
    } else {
      return null;
    }
  }

  @Override
  public final boolean supports(Class<?> authentication) {
    return isEnabled() && getDelegate().supports(authentication);
  }

}
