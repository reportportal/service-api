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

/**
 * Builds custom exception strings from Exception. Lots of exceptions have custom data which should be shown to the
 * clients in a custom way.
 *
 * @author Andrei Varabyeu
 */
public interface ExceptionMessageBuilder<T extends Exception> {

  /**
   * Builds message string from provided exception.
   *
   * @param e Exception message should be built from
   * @return Built message
   */
  String buildMessage(T e);
}
