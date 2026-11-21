/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.exception.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.exception.ExceptionMappings;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

class DefaultErrorResolverTest {

  private final DefaultErrorResolver resolver = new DefaultErrorResolver(ExceptionMappings.DEFAULT_MAPPING);

  @Test
  void handlerMethodValidationExceptionIsMappedLikeMethodArgumentNotValid() throws Exception {
    var result1 = mock(ParameterValidationResult.class);
    when(result1.getResolvableErrors()).thenReturn(List.of(
        new DefaultMessageSourceResolvable(new String[] {"c1"}, "first message"),
        new DefaultMessageSourceResolvable(new String[] {"c2"}, "second message")));
    var result2 = mock(ParameterValidationResult.class);
    when(result2.getResolvableErrors()).thenReturn(
        List.of(new DefaultMessageSourceResolvable(new String[] {"c3"}, "third message")));
    var ex = mock(HandlerMethodValidationException.class);
    when(ex.getParameterValidationResults()).thenReturn(List.of(result1, result2));

    var error = resolver.resolveError(ex);

    assertNotNull(error);
    assertEquals(400, error.httpStatus().value());
    assertEquals(ErrorType.INCORRECT_REQUEST, error.errorRs().getErrorType());
    assertEquals("Incorrect Request. [first message] [second message] [third message] ",
        error.errorRs().getMessage());
  }

  @Test
  void methodArgumentNotValidExceptionKeepsFormat() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
    bindingResult.addError(new FieldError("obj", "name", null, false, new String[] {"code"}, null, "name is bad"));
    var ex = new MethodArgumentNotValidException(
        new org.springframework.core.MethodParameter(Object.class.getMethod("toString"), -1), bindingResult);

    var error = resolver.resolveError(ex);

    assertEquals(400, error.httpStatus().value());
    assertEquals("Incorrect Request. [name is bad] ", error.errorRs().getMessage());
  }
}
