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

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Several validation checks
 *
 * @author Andrei Varabyeu
 */
public class Preconditions {

  /**
   * grabbed from {@link UUID#fromString(String)}
   */
  public static final Predicate<String> IS_UUID = uuid -> uuid.split("-").length == 5;
  public static final Predicate<Collection<?>> NOT_EMPTY_COLLECTION = t -> null != t
      && !t.isEmpty();
  public static final Predicate<Optional<?>> IS_PRESENT = Optional::isPresent;
  public static final Predicate<FilterCondition> HAS_ANY_MODE = hasMode(null);

  private Preconditions() {

  }

  public static Predicate<Instant> sameTimeOrLater(final Instant than) {
    com.google.common.base.Preconditions.checkNotNull(than, ErrorType.BAD_REQUEST_ERROR);
    return date -> date.isAfter(than) || date.equals(than);
  }

  public static Predicate<StatusEnum> statusIn(final StatusEnum... statuses) {
    return input -> ArrayUtils.contains(statuses, input);
  }

  public static Predicate<FilterCondition> hasMode(final LaunchModeEnum mode) {
    return condition -> (CRITERIA_LAUNCH_MODE.equalsIgnoreCase(condition.getSearchCriteria())) && (
        mode == null || mode.name().equalsIgnoreCase(condition.getValue()));
  }

  /**
   * Checks whether iterable contains elements matchers provided predicate
   *
   * @param filter
   * @return
   */
  public static <T> Predicate<Iterable<T>> contains(final Predicate<T> filter) {
    return iterable -> StreamSupport.stream(iterable.spliterator(), false).anyMatch(filter);
  }

  /**
   * Checks whether map contains provided key
   *
   * @param key
   * @return
   */
  public static <K> Predicate<Map<K, ?>> containsKey(final K key) {
    return map -> null != map && map.containsKey(key);
  }

  /**
   * Check whether user (principal) has enough role level
   *
   * @param principalRole
   * @return
   */
  public static Predicate<ProjectRole> isLevelEnough(final ProjectRole principalRole) {
    return principalRole::sameOrHigherThan;
  }
}
