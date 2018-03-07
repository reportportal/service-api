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
package com.epam.ta.reportportal.auth.basic;

import com.epam.reportportal.auth.event.UiAuthenticationFailureEventHandler;
import com.epam.ta.reportportal.auth.event.UiUserSignedInEvent;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

/**
 * Checks whether client have more auth errors than defined and throws exception if so
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

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		boolean accountNonLocked = !failureEventHandler.isBlocked(request.get());
		if (!accountNonLocked) {
			BusinessRule.fail().withError(ErrorType.ADDRESS_LOCKED);
		}

		Authentication auth = super.authenticate(authentication);
		eventPublisher.publishEvent(new UiUserSignedInEvent(auth));
		return auth;
	}
}