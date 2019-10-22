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

package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.reportportal.entity.user.UserRole;
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
			Optional<UserRole> userRole = UserRole.findByAuthority(
					authentication.getAuthorities().iterator().next().getAuthority());
			return userRole.isPresent() ? userRole.get() : WebArgumentResolver.UNRESOLVED;
		}
		return WebArgumentResolver.UNRESOLVED;
	}

}
