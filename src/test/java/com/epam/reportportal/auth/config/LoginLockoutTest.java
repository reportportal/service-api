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

package com.epam.reportportal.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.auth.DelegatingPluginAuthenticationProvider;
import com.epam.reportportal.auth.basic.BasicPasswordAuthenticationProvider;
import com.epam.reportportal.auth.event.UiAuthenticationFailureEventHandler;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Goes through the real login {@link AuthenticationManager} (not the failure handler directly), so it fails if nothing
 * publishes the bad-credentials event that feeds the lockout counter.
 */
class LoginLockoutTest {

  private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sso/oauth/token");
  private final Provider<HttpServletRequest> requestProvider = () -> request;
  private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private UiAuthenticationFailureEventHandler failureHandler;
  private AuthorizationServerConfig config;

  @BeforeEach
  void setUp() {
    request.setRemoteAddr("192.168.0.5");
    failureHandler = new UiAuthenticationFailureEventHandler();
    ReflectionTestUtils.setField(failureHandler, "request", requestProvider);
    when(passwordEncoder.encode(any())).thenReturn("encoded");
    when(passwordEncoder.matches(any(), any())).thenReturn(false);
    when(userDetailsService.loadUserByUsername("known")).thenReturn(
        User.withUsername("known").password("hash").authorities("ROLE_USER").build());
    when(userDetailsService.loadUserByUsername("ghost")).thenThrow(new UsernameNotFoundException("no"));

    config = new AuthorizationServerConfig(null, null, null, passwordEncoder, userDetailsService, null,
        new DelegatingPluginAuthenticationProvider(mock(Pf4jPluginBox.class)), null);
  }

  /**
   * Stands in for the Spring context: delivers events to the real listener.
   */
  private ApplicationEventPublisher publisher() {
    return event -> {
      if (event instanceof AuthenticationFailureBadCredentialsEvent e) {
        failureHandler.onApplicationEvent(e);
      }
    };
  }

  private BasicPasswordAuthenticationProvider basic(ApplicationEventPublisher publisher) {
    return new BasicPasswordAuthenticationProvider(userDetailsService, passwordEncoder, publisher, failureHandler,
        requestProvider);
  }

  /** The production manager, built by the production configuration. */
  private AuthenticationManager manager() {
    var publisher = publisher();
    return config.loginAuthenticationManager(basic(publisher), publisher);
  }

  /** Same wiring, with an extra plugin-style provider after the basic one. */
  private AuthenticationManager managerWith(AuthenticationProvider extra) {
    var publisher = publisher();
    var providers = new ArrayList<AuthenticationProvider>(List.of(basic(publisher), extra));
    var manager = new ProviderManager(providers);
    manager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(publisher));
    return manager;
  }

  private static Authentication login(String user) {
    return new UsernamePasswordAuthenticationToken(user, "wrong");
  }

  private static void assertBlockedAfterFourFailures(AuthenticationManager manager, String user) {
    // Exactly one count per failed login: a double count would block after two failures.
    for (int i = 0; i < 4; i++) {
      assertThrows(BadCredentialsException.class, () -> manager.authenticate(login(user)));
    }
    var ex = assertThrows(ReportPortalException.class, () -> manager.authenticate(login(user)));
    assertEquals(ErrorType.ADDRESS_LOCKED, ex.getErrorType());
  }

  @Test
  void wrongPasswordLocksOutAfterFailures() {
    assertBlockedAfterFourFailures(manager(), "known");
  }

  @Test
  void unknownUserIsCountedOnce() {
    assertBlockedAfterFourFailures(manager(), "ghost");
  }

  @Test
  void pluginProviderFailureIsCountedOnce() {
    AuthenticationProvider ldap = new AuthenticationProvider() {
      @Override
      public Authentication authenticate(Authentication authentication) {
        throw new BadCredentialsException("ldap bind failed");
      }

      @Override
      public boolean supports(Class<?> authentication) {
        return true;
      }
    };
    assertBlockedAfterFourFailures(managerWith(ldap), "known");
  }
}
