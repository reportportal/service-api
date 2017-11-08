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

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Wraps {@link HandlerMethodReturnValueHandler}. Checks if {@link ResponseView}
 * annotation present, and if yes wraps bean to be serialized with view mapped
 * to it on controller's level class
 *
 * @author Andrei Varabyeu
 */
class JacksonViewReturnValueHandler implements HandlerMethodReturnValueHandler {

	private final HandlerMethodReturnValueHandler delegate;

	public JacksonViewReturnValueHandler(HandlerMethodReturnValueHandler delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return delegate.supportsReturnType(returnType);
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {

		/*
		 * Wraps bean to be serialized if there is some view assigned to it on
		 * controller level
		 */
		Class<?> viewClass = getDeclaredViewClass(returnType);
		if (viewClass != null) {
			returnValue = wrapResult(returnValue, viewClass);
		}

		delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest);

	}

	/**
	 * Returns assigned view or null
	 *
	 * @param returnType
	 * @return
	 */
	private Class<?> getDeclaredViewClass(MethodParameter returnType) {
		ResponseView annotation = returnType.getMethodAnnotation(ResponseView.class);
		if (annotation != null) {
			return annotation.value();
		} else {
			return null;
		}
	}

	/**
	 * Wraps bean and view into one object
	 */
	private Object wrapResult(Object result, Class<?> viewClass) {
		return new JacksonViewAware(result, viewClass);
	}

}