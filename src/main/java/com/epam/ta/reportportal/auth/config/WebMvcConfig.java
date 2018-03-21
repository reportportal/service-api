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
package com.epam.ta.reportportal.auth.config;

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.commons.ExceptionMappings;
import com.epam.ta.reportportal.commons.exception.message.DefaultExceptionMessageBuilder;
import com.epam.ta.reportportal.commons.exception.rest.DefaultErrorResolver;
import com.epam.ta.reportportal.commons.exception.rest.ReportPortalExceptionResolver;
import com.epam.ta.reportportal.commons.exception.rest.RestErrorDefinition;
import com.epam.ta.reportportal.commons.exception.rest.RestExceptionHandler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private HttpMessageConverters messageConverters;

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new DatabaseUserDetailsService();
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {

		RestErrorDefinition<Exception> authErrorDefinition = new RestErrorDefinition<>(HttpStatus.BAD_REQUEST,
				ErrorType.ACCESS_DENIED,
				new DefaultExceptionMessageBuilder()
		);

		Map<Class<? extends Throwable>, RestErrorDefinition> errorMappings = ImmutableMap.<Class<? extends Throwable>, RestErrorDefinition>builder()
				.put(OAuth2Exception.class, authErrorDefinition)
				.put(AuthenticationException.class, authErrorDefinition)
				.put(UsernameNotFoundException.class, authErrorDefinition)
				.putAll(ExceptionMappings.DEFAULT_MAPPING)
				.build();

		RestExceptionHandler handler = new RestExceptionHandler();
		handler.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		handler.setErrorResolver(new ReportPortalExceptionResolver(new DefaultErrorResolver(errorMappings)));
		handler.setMessageConverters(messageConverters.getConverters());
		exceptionResolvers.add(handler);
	}

}
