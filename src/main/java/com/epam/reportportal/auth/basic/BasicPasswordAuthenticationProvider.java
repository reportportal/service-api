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

package com.epam.reportportal.auth.basic;

import com.epam.reportportal.auth.TokenServicesFacade;
import com.epam.reportportal.auth.config.password.ClientToken;
import com.epam.reportportal.auth.config.utils.ConvertToOauthToken;
import com.epam.reportportal.base.auth.event.UiAuthenticationFailureEventHandler;
import com.epam.reportportal.base.auth.event.UiUserSignedInEvent;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Checks whether client have more auth errors than defined and throws exception if so.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class BasicPasswordAuthenticationProvider extends DaoAuthenticationProvider {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private UiAuthenticationFailureEventHandler failureEventHandler;

  @Autowired
  private Provider<HttpServletRequest> request;

  @Autowired
  private TokenServicesFacade tokenService;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    boolean accountNonLocked = !failureEventHandler.isBlocked(request.get());
    if (!accountNonLocked) {
      throw new ReportPortalException(ErrorType.ADDRESS_LOCKED);
    }

    try {
      Authentication auth = super.authenticate(authentication);
      eventPublisher.publishEvent(new UiUserSignedInEvent(auth));

      ConvertToOauthToken convertToOauthToken = new ConvertToOauthToken(tokenService);

      ClientToken clientToken = (ClientToken) authentication;
      return convertToOauthToken.convert(clientToken, auth);
    } catch (UsernameNotFoundException e) {
      throw new BadCredentialsException("Bad credentials");
    }
  }
}
