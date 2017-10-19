/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
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

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import com.epam.ta.reportportal.ws.resolver.ActiveUserWebArgumentResolver;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/**
 * Argument resolver tests
 *
 * @author Andrei Varabyeu
 */
public class ActiveUserWebArgumentResolverTest {

	private HandlerMethodArgumentResolver argumentResolver = new ActiveUserWebArgumentResolver();

	@Test
	public void testSupports() throws SecurityException, NoSuchMethodException {

		MethodParameter mp = new MethodParameter(this.getClass().getMethod("correctMethod", UserRole.class), 0);
		Assert.assertTrue("@ActiveRole annotation isn't resolved", argumentResolver.supportsParameter(mp));
	}

	@Test
	public void testNotSupportsWithoutAnnotation() throws SecurityException, NoSuchMethodException {

		MethodParameter mp = new MethodParameter(this.getClass().getMethod("incorrectMethodWithoutAnnotation", UserRole.class), 0);
		Assert.assertFalse("Parameter is resolved without annotation", argumentResolver.supportsParameter(mp));
	}

	@Test
	public void testNotSupportsWithIncorrectType() throws SecurityException, NoSuchMethodException {
		MethodParameter mp = new MethodParameter(this.getClass().getMethod("incorrectMethodWithIncorrectType", String.class), 0);
		Assert.assertFalse("Parameter resolved with incorrect type", argumentResolver.supportsParameter(mp));
	}

	public void correctMethod(@ActiveRole UserRole activeRole) {
		// do nothing
	}

	public void incorrectMethodWithoutAnnotation(UserRole activeRole) {
		// do nothing
	}

	public void incorrectMethodWithIncorrectType(@ActiveRole String activeRole) {
		// do nothing
	}
}