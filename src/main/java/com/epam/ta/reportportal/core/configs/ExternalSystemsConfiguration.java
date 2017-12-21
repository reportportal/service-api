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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.commons.exception.forwarding.ForwardingClientExceptionHandler;
import com.epam.ta.reportportal.core.externalsystem.ExternalSystemEurekaDelegate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Configuration class for external systems
 *
 * @author Andrei Varabyeu
 */
@Configuration
public class ExternalSystemsConfiguration {

	@Bean
	public ExternalSystemEurekaDelegate externalSystemStrategy() {
		return new ExternalSystemEurekaDelegate(restTemplate());
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate(Collections.singletonList(new MappingJackson2HttpMessageConverter()));
		restTemplate.setErrorHandler(new ForwardingClientExceptionHandler());
		return restTemplate;
	}
}