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

package com.epam.reportportal.base.infrastructure.persistence.commons;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author dzmitry_kavalets
 */
public class Predicates {

  private Predicates() {
    //statics only
  }

  public static <T> Predicate<T> notNull() {
    return Objects::nonNull;
  }

  public static <T> Predicate<T> isNull() {
    return Objects::isNull;
  }

  public static <T> Predicate<T> equalTo(T target) {
    return (target == null) ? Predicates.isNull() : t -> t.equals(target);
  }

  public static <T> Predicate<T> not(Predicate<T> predicate) {
    return item -> !predicate.test(item);
  }

  public static <T> Predicate<T> in(Collection<? extends T> target) {
    return target::contains;
  }

  public static <T> Predicate<T> alwaysFalse() {
    return t -> false;
  }

  public static <T> Predicate<T> and(List<? extends Predicate<? super T>> components) {
    return t -> components.stream().allMatch(predicate -> predicate.test(t));
  }

  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> or(Predicate<? super T>... components) {
    return t -> Stream.of(components).anyMatch(predicate -> predicate.test(t));
  }

  public static <T> Predicate<T> or(Iterable<? extends Predicate<? super T>> components) {
    return t -> StreamSupport.stream(components.spliterator(), false)
        .anyMatch(predicate -> predicate.test(t));
  }

  public static Predicate<Optional<?>> isPresent() {
    return Optional::isPresent;
  }
}
