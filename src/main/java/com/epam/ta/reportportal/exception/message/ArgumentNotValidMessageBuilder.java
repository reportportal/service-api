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

package com.epam.ta.reportportal.exception.message;

import java.util.stream.Collectors;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * {@link MethodArgumentNotValidException} message builder
 *
 * @author Andrei Varabyeu
 */
public class ArgumentNotValidMessageBuilder implements ExceptionMessageBuilder<MethodArgumentNotValidException> {

  private final ReloadableResourceBundleMessageSource messageSource;

  public ArgumentNotValidMessageBuilder(ReloadableResourceBundleMessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public String buildMessage(MethodArgumentNotValidException e) {
    return e.getBindingResult().getAllErrors().stream()
        .map(err -> messageSource.getMessage(err, LocaleContextHolder.getLocale()))
        .collect(Collectors.joining("", "[", "] "));
  }


}
