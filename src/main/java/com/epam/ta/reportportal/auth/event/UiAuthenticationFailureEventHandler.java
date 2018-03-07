/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.auth.event;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.net.HttpHeaders;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Initial implementation of authentication failures handler
 *
 * @author Andrei_Ramanchuk
 */
@Component
public class UiAuthenticationFailureEventHandler implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

	private static final long MAXIMUM_SIZE = 5000;
	private static final long EXPIRATION_SECONDS = 30;
	private static final int MAX_ATTEMPTS = 3;
	private static final RequestHeaderRequestMatcher AJAX_REQUEST_MATCHER = new RequestHeaderRequestMatcher(HttpHeaders.X_REQUESTED_WITH,
			"XMLHttpRequest");

	@Inject
	private Provider<HttpServletRequest> request;

	private LoadingCache<String, AtomicInteger> failures;

	public UiAuthenticationFailureEventHandler() {
		super();
		failures = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(EXPIRATION_SECONDS, TimeUnit.SECONDS)
				.build(new CacheLoader<String, AtomicInteger>() {
					@Override
					public AtomicInteger load(String key) {
						return new AtomicInteger(0);
					}
				});
	}

	public boolean isBlocked(HttpServletRequest request) {
		AtomicInteger attempts = failures.getIfPresent(getClientIP(request));
		return null != attempts && attempts.get() > MAX_ATTEMPTS;
	}

	private void onAjaxFailure(HttpServletRequest request) {
		String clientIP = getClientIP(request);
		failures.getUnchecked(clientIP).incrementAndGet();

	}

	private String getClientIP(HttpServletRequest request) {
		String xfHeader = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
		if (xfHeader == null) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}

	@Override
	public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
		onAjaxFailure(request.get());
	}
}