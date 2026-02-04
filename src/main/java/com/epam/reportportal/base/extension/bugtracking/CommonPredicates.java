/*
 * Copyright 2018 EPAM Systems
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

package com.epam.reportportal.base.extension.bugtracking;


import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Some common {@link Predicate}
 *
 * @author Andrei Varabyeu
 */
public final class CommonPredicates {

  private CommonPredicates() {
    //static only
  }

  public static final Predicate<Collection<?>> IS_EMPTY = input -> null == input || input.isEmpty();

  public static final Predicate<Map<?, ?>> IS_MAP_EMPTY = input -> null == input || input.isEmpty();
}
