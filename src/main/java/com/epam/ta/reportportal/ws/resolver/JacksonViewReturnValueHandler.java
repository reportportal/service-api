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

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Wraps {@link HandlerMethodReturnValueHandler}. Checks if {@link ResponseView} annotation present,
 * and if yes wraps bean to be serialized with view mapped to it on controller's level class
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
  public void handleReturnValue(Object returnValue, MethodParameter returnType,
      ModelAndViewContainer mavContainer,
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