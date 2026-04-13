/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.base.core.item;

import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asynchronously updates {@code last_modified} timestamp on {@code test_item} rows when a launch
 * attribute that affects item visibility changes (e.g. mode switch).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestItemLastModifiedService {

  private final TestItemRepository testItemRepository;

  /**
   * Sets {@code last_modified = CURRENT_TIMESTAMP} for every {@code test_item} that belongs to the
   * given launch. Runs asynchronously on the {@code eventListenerExecutor} thread pool so it does
   * not participate in the caller's transaction and cannot contribute to deadlocks.
   *
   * @param launchId the launch whose items should be touched
   */
  @Async("eventListenerExecutor")
  @Transactional
  public void updateByLaunchId(Long launchId) {
    testItemRepository.updateLastModifiedByLaunchId(launchId);
  }
}
