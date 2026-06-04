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

public abstract class RuleValidator<T> {

  protected final Predicate<T> predicate;
  protected final T target;

  public RuleValidator(T target, Predicate<T> predicate) {
    this.target = target;
    this.predicate = predicate;

  }

}
