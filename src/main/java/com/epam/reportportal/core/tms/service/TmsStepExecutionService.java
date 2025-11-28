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

package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.dao.TmsStepExecutionRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsStepExecution;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing TmsStepExecution records. Tracks executions of nested steps created from
 * manual scenarios.
 *
 * @author ReportPortal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsStepExecutionService {

  private final TmsStepExecutionRepository tmsStepExecutionRepository;

  /**
   * Creates TmsStepExecution records for all nested steps under a test item.
   *
   * @param testCaseExecutionId parent test case execution ID
   * @param nestedSteps         list of nested step test items
   * @param launch              launch entity
   * @param tmsStepIds          optional list of TMS step IDs (mapped 1:1 to nestedSteps if
   *                            provided)
   */
  @Transactional
  public void createTmsStepExecutions(
      Long testCaseExecutionId,
      List<TestItem> nestedSteps,
      Launch launch,
      List<Long> tmsStepIds) {

    log.debug("Creating {} step execution records for test case execution: {}",
        nestedSteps.size(), testCaseExecutionId);

    if (nestedSteps.isEmpty()) {
      log.debug("No nested steps to create execution records for");
      return;
    }

    for (var i = 0; i < nestedSteps.size(); i++) {
      try {
        var nestedStep = nestedSteps.get(i);
        var tmsStepId = (tmsStepIds != null && i < tmsStepIds.size()) ? tmsStepIds.get(i) : null;

        var stepExecution = TmsStepExecution.builder()
            .testCaseExecutionId(testCaseExecutionId) //TODO move to mapper
            .testItem(nestedStep)
            .launchId(launch.getId())
            .tmsStepId(tmsStepId)
            .build();

        tmsStepExecutionRepository.save(stepExecution);
        log.trace("Created step execution for nested item: {} with TMS step ID: {}",
            nestedStep.getItemId(), tmsStepId);

      } catch (Exception e) {
        log.error("Error creating step execution record for nested step index: {}",
            i, e);
        throw e;  // Re-throw to stop transaction
      }
    }

    log.info("Successfully created {} step execution records", nestedSteps.size());
  }

  /**
   * Retrieves all step executions for a test case execution.
   *
   * @param testCaseExecutionId test case execution ID
   * @return list of step executions
   */
  @Transactional(readOnly = true)
  public List<TmsStepExecution> getStepExecutionsByTestCaseExecution(Long testCaseExecutionId) {
    log.debug("Retrieving step executions for test case execution: {}", testCaseExecutionId);
    return tmsStepExecutionRepository.findByTestCaseExecutionId(testCaseExecutionId);
  }

  /**
   * Deletes all step execution records for a test case execution.
   *
   * @param testCaseExecutionId test case execution ID
   */
  @Transactional
  public void deleteStepExecutionsByTestCaseExecution(Long testCaseExecutionId) {
    log.debug("Deleting step execution records for test case execution: {}", testCaseExecutionId);
    tmsStepExecutionRepository.deleteByTestCaseExecutionId(testCaseExecutionId);
    log.trace("Deleted step execution records");
  }

  @Transactional
  public void deleteByLaunchId(Long launchId) {
    tmsStepExecutionRepository.deleteByLaunchId(launchId);
  }
}
