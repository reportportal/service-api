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

package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing TmsTestFolderTestItem junction entities.
 *
 * @author ReportPortal
 */
public interface TmsTestFolderTestItemRepository extends JpaRepository<TmsTestFolderTestItem, Long> {

  /**
   * Finds junction record linking test folder and test item.
   *
   * @param testFolderId test folder ID
   * @param testItemId test item ID
   * @return Optional containing junction record if exists
   */
  @Query("SELECT tft FROM TmsTestFolderTestItem tft " +
         "WHERE tft.testFolder.id = :testFolderId AND tft.testItem.itemId = :testItemId")
  Optional<TmsTestFolderTestItem> findByTestFolderIdAndTestItemId(
      @Param("testFolderId") Long testFolderId,
      @Param("testItemId") Long testItemId);

  /**
   * Checks if junction record exists for test folder and test item.
   *
   * @param testFolderId test folder ID
   * @param testItemId test item ID
   * @return true if exists, false otherwise
   */
  @Query("SELECT COUNT(tft) > 0 FROM TmsTestFolderTestItem tft " +
         "WHERE tft.testFolder.id = :testFolderId AND tft.testItem.itemId = :testItemId")
  boolean existsByTestFolderIdAndTestItemId(
      @Param("testFolderId") Long testFolderId,
      @Param("testItemId") Long testItemId);

  /**
   * Finds test item ID for a test folder in a specific launch.
   * Returns SUITE item (parent item without parent) for the test folder.
   *
   * @param testFolderId test folder ID
   * @param launchId launch ID
   * @return Optional containing test item ID if SUITE item exists
   */
  @Query("SELECT tft.testItem.itemId FROM TmsTestFolderTestItem tft " +
         "WHERE tft.testFolder.id = :testFolderId " +
         "AND tft.testItem.launchId = :launchId " +
         "AND tft.testItem.parentId IS NULL")
  Optional<Long> findSuiteItemIdByTestFolderAndLaunch(
      @Param("testFolderId") Long testFolderId,
      @Param("launchId") Long launchId);

  /**
   * Deletes all junction records for a test item.
   *
   * @param testItemId test item ID
   */
  void deleteByTestItemId(Long testItemId);
}
