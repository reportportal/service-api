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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.test.util.ReflectionTestUtils;

class UiAuthenticationFailureEventHandlerTest {

  private final UiAuthenticationFailureEventHandler handler = new UiAuthenticationFailureEventHandler();
  private final AtomicReference<HttpServletRequest> currentRequest = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    Provider<HttpServletRequest> provider = currentRequest::get;
    ReflectionTestUtils.setField(handler, "request", provider);
  }

  private static MockHttpServletRequest request(String remoteAddr, String forwardedFor) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sso/oauth/token");
    request.setRemoteAddr(remoteAddr);
    if (forwardedFor != null) {
      request.addHeader("X-Forwarded-For", forwardedFor);
    }
    return request;
  }

  private void fail(HttpServletRequest request, String username) {
    currentRequest.set(request);
    handler.onApplicationEvent(new AuthenticationFailureBadCredentialsEvent(
        new UsernamePasswordAuthenticationToken(username, "wrong"), new BadCredentialsException("Bad credentials")));
  }

  @Test
  void rotatingForwardedForHeaderDoesNotEvadeAddressLimit() {
    for (int i = 0; i < 4; i++) {
      fail(request("192.168.0.5", "10.0.0." + i), "user" + i);
    }

    assertTrue(handler.isBlocked(request("192.168.0.5", "10.0.0.99"), "someone-else"));
  }

  @Test
  void forwardedForHeaderCannotLockOutAnotherAddress() {
    for (int i = 0; i < 4; i++) {
      fail(request("192.168.0.5", "192.168.0.7"), "user" + i);
    }

    assertFalse(handler.isBlocked(request("192.168.0.7", null), "victim"));
  }

  @Test
  void repeatedFailuresForOneUsernameFromDifferentAddressesBlockThatUsername() {
    for (int i = 0; i < 4; i++) {
      fail(request("192.168.0." + i, null), "Victim");
    }

    assertTrue(handler.isBlocked(request("192.168.0.100", null), "victim"));
    assertFalse(handler.isBlocked(request("192.168.0.100", null), "other"));
  }
}
