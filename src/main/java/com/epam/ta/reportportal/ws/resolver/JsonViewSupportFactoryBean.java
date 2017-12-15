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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Initializing bean for wrapping {@link HandlerMethodReturnValueHandler} with
 * JSON view decorators
 *
 * @author Andrei Varabyeu
 */
public class JsonViewSupportFactoryBean implements InitializingBean {

	@Autowired
	private RequestMappingHandlerAdapter adapter;

	@Override
	public void afterPropertiesSet() {
		List<HandlerMethodReturnValueHandler> handlers = adapter.getReturnValueHandlers();
		adapter.setReturnValueHandlers(decorateHandlers(handlers));
	}

	private List<HandlerMethodReturnValueHandler> decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {

		/*
		 * We have to create new collection here, because initial list is
		 * unmodifiable
		 */
		List<HandlerMethodReturnValueHandler> updatedHandlers = new ArrayList<>(handlers.size());
		for (HandlerMethodReturnValueHandler handler : handlers) {
			if (handler instanceof RequestResponseBodyMethodProcessor) {
				updatedHandlers.add(new JacksonViewReturnValueHandler(handler));
			} else {
				updatedHandlers.add(handler);
			}
		}
		return Collections.unmodifiableList(updatedHandlers);
	}
}
