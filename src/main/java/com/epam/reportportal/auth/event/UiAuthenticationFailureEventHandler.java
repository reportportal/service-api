/*
 * Copyright 2025 EPAM Systems
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

/**
 * Counts failed logins per client address and per username and blocks further attempts for a short window once either
 * exceeds the limit. The client address is {@link HttpServletRequest#getRemoteAddr()}, i.e. whatever the servlet
 * container / forwarded-header handling resolved, never a raw request header.
 *
 * @author Andrei_Ramanchuk
 */
@Component
public class UiAuthenticationFailureEventHandler implements
    ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

  private static final long MAXIMUM_SIZE = 5000;
  private static final long EXPIRATION_SECONDS = 30;
  private static final int MAX_ATTEMPTS = 3;

  @Inject
  private Provider<HttpServletRequest> request;

  private final LoadingCache<String, AtomicInteger> failures;

  private final LoadingCache<String, AtomicInteger> usernameFailures;

  public UiAuthenticationFailureEventHandler() {
    super();
    failures = newFailureCache();
    usernameFailures = newFailureCache();
  }

  private static LoadingCache<String, AtomicInteger> newFailureCache() {
    return CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE)
        .expireAfterWrite(Duration.ofSeconds(EXPIRATION_SECONDS))
        .build(new CacheLoader<>() {
          @Override
          public AtomicInteger load(String key) {
            return new AtomicInteger(0);
          }
        });
  }

  /**
   * @param request  the login request
   * @param username the login being authenticated, may be {@code null}
   * @return {@code true} if the client address or the username has too many recent failures
   */
  public boolean isBlocked(HttpServletRequest request, String username) {
    return exceeded(failures.getIfPresent(getClientIP(request)))
        || (username != null && exceeded(usernameFailures.getIfPresent(normalize(username))));
  }

  private static boolean exceeded(AtomicInteger attempts) {
    return null != attempts && attempts.get() > MAX_ATTEMPTS;
  }

  private void onAjaxFailure(HttpServletRequest request, String username) {
    failures.getUnchecked(getClientIP(request)).incrementAndGet();
    if (username != null) {
      usernameFailures.getUnchecked(normalize(username)).incrementAndGet();
    }
  }

  private static String getClientIP(HttpServletRequest request) {
    return request.getRemoteAddr();
  }

  private static String normalize(String username) {
    return username.trim().toLowerCase(Locale.ROOT);
  }

  @Override
  public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
    onAjaxFailure(request.get(), event.getAuthentication().getName());
  }
}
