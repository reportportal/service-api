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

package com.epam.reportportal.base.infrastructure.rules.commons.validation;


import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DefaultRuleValidator<T> extends RuleValidator<T> {

  private final Supplier<String> message;

  public DefaultRuleValidator(T target, Predicate<T> predicate,
      Supplier<String> message) {
    super(target, predicate);
    this.message = message;
  }

  /**
   * Verifies predicate and throws {@link BusinessRuleViolationException} is there is violation
   *
   * @throws BusinessRuleViolationException if rule condition is not passed
   */
  public void verify() throws BusinessRuleViolationException {
    if (!predicate.test(target)) {
      throw new BusinessRuleViolationException(message.get());
    }
  }

  /**
   * Verifies predicate and throws {@link ReportPortalException} if there is violation
   *
   * @param t - exception to thrown instead of {@link ReportPortalException}
   */
  public void verify(Class<? extends ReportPortalException> t) {
    if (!predicate.test(target)) {
      ReportPortalException toBeThrowed;
      try {
        toBeThrowed = t.getConstructor(String.class).newInstance(
            message.get());
      } catch (Exception e) {
        throw new ReportPortalException(message.get(), e);
      }
      throw toBeThrowed;
    }
  }

}
