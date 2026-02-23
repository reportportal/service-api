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

package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.core.item.repository.DeleteItemContext;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueType;

/**
 * Service for managing test item statistics directly via SQL, bypassing Hibernate. All operations
 * use advisory locks on launch_id to prevent deadlocks. Replaces the following DB trigger
 * functions:
 * <ul>
 *   <li>{@code update_executions_statistics} — handled by
 *       {@link #addExecutionStatistics} and {@link #changeExecutionStatistics}</li>
 *   <li>{@code increment_defect_statistics} — handled by {@link #addDefectStatistics}</li>
 *   <li>{@code update_defect_statistics} — handled by {@link #changeDefectStatistics}</li>
 *   <li>{@code delete_defect_statistics} — handled by {@link #removeDefectStatistics}</li>
 *   <li>{@code delete_item_statistics / decrease_statistics} — handled by
 *       {@link #deleteItemStatistics}</li>
 * </ul>
 * <p>All methods expect a valid {@link TestItem} with populated
 * {@code itemId}, {@code launchId}, and {@code path}.
 *
 * @author Pavel Bortnik
 */
public interface TestItemStatisticsService {

  void addStatistics(TestItem item);

  /**
   * Change defect statistics when issue type changes. Decrements old defect type fields and
   * increments new defect type fields by 1 for the item itself, all ancestors (by path) and the
   * launch.
   *
   * @param item         leaf test item
   * @param oldIssueType previous issue type (may be null)
   * @param newIssueType new issue type (may be null)
   */
  void changeDefectStatistics(TestItem item, IssueType oldIssueType, IssueType newIssueType);

  /**
   * Subtracts all item's non-zero counters from ancestors and launch, then removes the item's own
   * statistics rows.
   * <p>Note: this method does NOT delete the issue record;
   * the caller should handle issue deletion separately.
   *
   * @param item leaf test item
   */
  void deleteItemStatistics(DeleteItemContext item);
}
