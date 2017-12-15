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

package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.reportportal.database.entity.user.UserRole;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

/**
 * {@link org.springframework.web.bind.support.WebArgumentResolver} for
 * ReportPortal User Roles. Will resolve {@link UserRole} in case if method
 * parameter annotated by {@link ActiveRole} annotation
 *
 * @author Andrei Varabyeu
 */
public class ActiveUserWebArgumentResolver implements HandlerMethodArgumentResolver {

	/**
	 * Returns TRUE if method argument is {@link UserRole} and annotated by
	 * {@link ActiveRole} annotation
	 */
	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return methodParameter.getParameterType().equals(UserRole.class) && null != methodParameter.getParameterAnnotation(
				ActiveRole.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.method.support.HandlerMethodArgumentResolver#
	 * resolveArgument(org.springframework.core.MethodParameter,
	 * org.springframework.web.method.support.ModelAndViewContainer,
	 * org.springframework.web.context.request.NativeWebRequest,
	 * org.springframework.web.bind.support.WebDataBinderFactory)
	 */
	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer paramModelAndViewContainer,
			NativeWebRequest webRequest, WebDataBinderFactory paramWebDataBinderFactory) {
		Authentication authentication = (Authentication) webRequest.getUserPrincipal();
		if (!authentication.getAuthorities().isEmpty()) {
			Optional<UserRole> userRole = UserRole.findByAuthority(authentication.getAuthorities().iterator().next().getAuthority());
			return userRole.isPresent() ? userRole.get() : WebArgumentResolver.UNRESOLVED;
		}
		return WebArgumentResolver.UNRESOLVED;
	}

}
