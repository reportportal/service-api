/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.exception;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.exception.message.ArgumentNotValidMessageBuilder;
import com.epam.ta.reportportal.exception.message.DefaultExceptionMessageBuilder;
import com.epam.ta.reportportal.exception.message.ExceptionMessageBuilder;
import com.epam.ta.reportportal.exception.rest.RestErrorDefinition;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Set of default exception mappings.
 *
 * @author Andrei Varabyeu
 */
public final class ExceptionMappings {

  public static final ReloadableResourceBundleMessageSource MESSAGE_SOURCE = new ReloadableResourceBundleMessageSource() {
    {
      setBasename("classpath:ValidationMessages");
      setDefaultEncoding("UTF-8");
    }
  };

  private static final ExceptionMessageBuilder<Exception> DEFAULT_MESSAGE_BUILDER = new DefaultExceptionMessageBuilder();

  public static Map<Class<? extends Throwable>, RestErrorDefinition> DEFAULT_MAPPING = ImmutableMap
      .<Class<? extends Throwable>, RestErrorDefinition>builder()
      .put(MethodArgumentNotValidException.class, new RestErrorDefinition<>(400, ErrorType.INCORRECT_REQUEST,
          new ArgumentNotValidMessageBuilder(MESSAGE_SOURCE)))
      .put(HttpMessageNotReadableException.class,
          new RestErrorDefinition<>(400, ErrorType.INCORRECT_REQUEST, DEFAULT_MESSAGE_BUILDER))
      .put(MissingServletRequestPartException.class,
          new RestErrorDefinition<>(400, ErrorType.INCORRECT_REQUEST, DEFAULT_MESSAGE_BUILDER))
      .put(MissingServletRequestParameterException.class,
          new RestErrorDefinition<>(400, ErrorType.INCORRECT_REQUEST, DEFAULT_MESSAGE_BUILDER))
      .put(AccessDeniedException.class,
          new RestErrorDefinition<>(403, ErrorType.ACCESS_DENIED, DEFAULT_MESSAGE_BUILDER))
      .put(BadCredentialsException.class,
          new RestErrorDefinition<>(401, ErrorType.ACCESS_DENIED, DEFAULT_MESSAGE_BUILDER))
      .put(LockedException.class, new RestErrorDefinition<>(403, ErrorType.ADDRESS_LOCKED, DEFAULT_MESSAGE_BUILDER))
      .put(RestClientException.class,
          new RestErrorDefinition<>(400, ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, DEFAULT_MESSAGE_BUILDER))
      .put(Throwable.class, new RestErrorDefinition<>(500, ErrorType.UNCLASSIFIED_ERROR, DEFAULT_MESSAGE_BUILDER))
      .build();
}
