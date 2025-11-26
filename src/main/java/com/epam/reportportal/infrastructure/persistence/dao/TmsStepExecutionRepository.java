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

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsStepExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing TmsStepExecution entities.
 *
 * @author ReportPortal
 */
public interface TmsStepExecutionRepository extends JpaRepository<TmsStepExecution, Long> {

  /**
   * Finds all step executions for a specific test case execution.
   *
   * @param testCaseExecutionId test case execution ID
   * @return list of step executions
   */
  @Query("SELECT tse FROM TmsStepExecution tse " +
         "WHERE tse.testCaseExecutionId = :testCaseExecutionId " +
         "ORDER BY tse.createdAt ASC")
  List<TmsStepExecution> findByTestCaseExecutionId(
      @Param("testCaseExecutionId") Long testCaseExecutionId);

  /**
   * Finds a step execution by test item ID.
   *
   * @param testItemId test item ID
   * @return Optional containing step execution if found
   */
  @Query("SELECT tse FROM TmsStepExecution tse " +
         "WHERE tse.testItem.itemId = :testItemId")
  Optional<TmsStepExecution> findByTestItemId(@Param("testItemId") Long testItemId);

  /**
   * Checks if a step execution exists for a test item.
   *
   * @param testItemId test item ID
   * @return true if exists, false otherwise
   */
  @Query("SELECT COUNT(tse) > 0 FROM TmsStepExecution tse " +
         "WHERE tse.testItem.itemId = :testItemId")
  boolean existsByTestItemId(@Param("testItemId") Long testItemId);

  /**
   * Deletes all step executions for a test case execution.
   *
   * @param testCaseExecutionId test case execution ID
   */
  void deleteByTestCaseExecutionId(Long testCaseExecutionId);
}
