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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

/**
 * Initializing bean for wrapping {@link HandlerMethodReturnValueHandler} with JSON view decorators
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

  private List<HandlerMethodReturnValueHandler> decorateHandlers(
      List<HandlerMethodReturnValueHandler> handlers) {

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
