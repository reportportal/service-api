/*
 * Copyright 2017 EPAM Systems
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
package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.auth.store.AuthConfigRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Dynamic (enableable) auth provider
 *
 * @author Andrei Varabyeu
 */
public abstract class EnableableAuthProvider {
//public abstract class EnableableAuthProvider implements AuthenticationProvider {
//
//	protected final AuthConfigRepository authConfigRepository;
//
//	protected EnableableAuthProvider(AuthConfigRepository authConfigRepository) {
//		this.authConfigRepository = authConfigRepository;
//	}
//
//	protected abstract boolean isEnabled();
//
//	protected abstract AuthenticationProvider getDelegate();
//
//	@Override
//	public final Authentication authenticate(Authentication authentication) throws AuthenticationException {
//		return isEnabled() ? getDelegate().authenticate(authentication) : null;
//	}
//
//	@Override
//	public final boolean supports(Class<?> authentication) {
//		return isEnabled() && getDelegate().supports(authentication);
//	}

}
