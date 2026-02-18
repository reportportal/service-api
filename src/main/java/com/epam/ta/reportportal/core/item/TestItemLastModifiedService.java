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

package com.epam.ta.reportportal.core.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronously updates {@code last_modified} timestamp on {@code test_item} rows when a launch
 * attribute that affects item visibility changes (e.g. mode switch).
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestItemLastModifiedService {

  private final JdbcTemplate jdbcTemplate;

  /**
   * Updates {@code last_modified = CURRENT_TIMESTAMP} for every {@code test_item} that belongs to
   * the given launch. Runs asynchronously on the {@code eventListenerExecutor} thread pool so it
   * does not participate in the caller's transaction.
   *
   * @param launchId the launch whose items should be touched
   */
  @Async("eventListenerExecutor")
  public void updateByLaunchId(Long launchId) {
    try {
      jdbcTemplate.update("""
          UPDATE test_item
          SET last_modified = CURRENT_TIMESTAMP
          WHERE launch_id = ?
          """, launchId);
    } catch (Exception e) {
      log.error("Failed to update last_modified for test items of launch {}", launchId, e);
    }
  }
}
