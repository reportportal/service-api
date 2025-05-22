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


import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of {@link ExceptionMessageBuilder} for formatting exception messages. This implementation
 * simplifies multi-line exception messages by extracting only the first line of the message text (content before the
 * first newline character) using {@link StringUtils#substringBefore}.
 */
public class DefaultExceptionMessageBuilder implements ExceptionMessageBuilder<Exception> {

  /**
   * Builds a simplified message from the given exception by extracting only the first line.
   *
   * @param e the exception whose message needs to be processed
   * @return the part of exception message before the first newline character, or null if the message is null
   */
  @Override
  public String buildMessage(Exception e) {
    return StringUtils.substringBefore(e.getMessage(), "\n");
  }

}
