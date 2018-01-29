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

import com.epam.ta.reportportal.ws.resolver.JacksonViewAwareModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Andrei Varabyeu
 */
@Configuration
public class JacksonConfiguration {

	/**
	 * @return
	 */
	@Bean(name = "objectMapper")
	public ObjectMapper objectMapper() {
		ObjectMapper om = new ObjectMapper();
		om.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
		om.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		om.registerModule(new JacksonViewAwareModule(om));
		return om;
	}
}
