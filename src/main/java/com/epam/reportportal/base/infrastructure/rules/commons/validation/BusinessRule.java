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


import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Domain objects verification utility, based on predicates as business rules.
 *
 * @author Dzianis Shlychkou
 */
public class BusinessRule {

  private BusinessRule() {
    //should be created by factory-methods only
  }

  /**
   * Create rule from object to be verified, predicate and error message in case of violation
   *
   * @param <T>       Type of object being checked
   * @param object    Object to be validated
   * @param predicate Validation predicate
   * @param message   Error message
   * @return Validator
   * @deprecated in favor of {@link #expect(Object, Predicate, Supplier)}<br> This approach would be better in case you
   * need to concatenate some error string
   */
  @Deprecated
  public static <T> DefaultRuleValidator<T> expect(T object, Predicate<T> predicate,
      String message) {
    return new DefaultRuleValidator<>(object, predicate, Suppliers.stringSupplier(message));
  }

  /**
   * Create rule from object to be verified, predicate and error message in case of violation
   *
   * @param <T>             Type of object being checked
   * @param object          Object to be validated
   * @param predicate       Validation predicate
   * @param messageSupplier Error message supplier
   * @return Validator
   */
  public static <T> DefaultRuleValidator<T> expect(T object, Predicate<T> predicate,
      Supplier<String> messageSupplier) {
    return new DefaultRuleValidator<>(object, predicate, messageSupplier);
  }

  /**
   * Create rule from object to be verified, predicate
   *
   * @param <T>       Type of object being checked
   * @param object    Object to be validated
   * @param predicate Validation predicate
   * @return Validator
   */
  public static <T> ErrorTypeBasedRuleValidator<T> expect(T object, Predicate<T> predicate) {
    return new ErrorTypeBasedRuleValidator<>(object, predicate);
  }

  /**
   * For cases where we are going to fail something
   *
   * @return {@link AlwaysFailRuleValidator}
   */
  public static AlwaysFailRuleValidator fail() {
    return new AlwaysFailRuleValidator();
  }
}
