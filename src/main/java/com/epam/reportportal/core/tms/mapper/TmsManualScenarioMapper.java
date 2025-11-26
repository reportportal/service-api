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

package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsManualScenarioRS;
import com.epam.reportportal.core.tms.dto.TmsStepsManualScenarioRS;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper utility for working with TMS manual scenarios.
 * Provides helper methods to identify and extract scenario data.
 *
 * @author ReportPortal
 */
@Slf4j
@Component
public class TmsManualScenarioMapper {

  /**
   * Checks if scenario is steps-based (TmsStepsManualScenarioRS).
   *
   * @param scenario manual scenario
   * @return true if steps-based, false otherwise
   */
  public boolean isStepsBasedScenario(TmsManualScenarioRS scenario) {
    return scenario instanceof TmsStepsManualScenarioRS;
  }

  /**
   * Checks if scenario is text-based (TmsTextManualScenarioRS).
   *
   * @param scenario manual scenario
   * @return true if text-based, false otherwise
   */
  public boolean isTextBasedScenario(TmsManualScenarioRS scenario) {
    return scenario instanceof TmsTextManualScenarioRS;
  }

  /**
   * Safely casts scenario to TmsStepsManualScenarioRS.
   *
   * @param scenario manual scenario
   * @return casted scenario or null if not instance of TmsStepsManualScenarioRS
   */
  public TmsStepsManualScenarioRS asStepsScenario(TmsManualScenarioRS scenario) {
    if (scenario instanceof TmsStepsManualScenarioRS) {
      return (TmsStepsManualScenarioRS) scenario;
    }
    log.warn("Scenario is not TmsStepsManualScenarioRS, returning null");
    return null;
  }

  /**
   * Safely casts scenario to TmsTextManualScenarioRS.
   *
   * @param scenario manual scenario
   * @return casted scenario or null if not instance of TmsTextManualScenarioRS
   */
  public TmsTextManualScenarioRS asTextScenario(TmsManualScenarioRS scenario) {
    if (scenario instanceof TmsTextManualScenarioRS) {
      return (TmsTextManualScenarioRS) scenario;
    }
    log.warn("Scenario is not TmsTextManualScenarioRS, returning null");
    return null;
  }

  /**
   * Validates that scenario has required data.
   *
   * @param scenario manual scenario
   * @return true if scenario has data, false otherwise
   */
  public boolean isValidScenario(TmsManualScenarioRS scenario) {
    if (scenario instanceof TmsStepsManualScenarioRS) {
      var stepsScenario = (TmsStepsManualScenarioRS) scenario;
      return stepsScenario.getSteps() != null && !stepsScenario.getSteps().isEmpty();
    } else if (scenario instanceof TmsTextManualScenarioRS) {
      var textScenario = (TmsTextManualScenarioRS) scenario;
      return textScenario.getInstructions() != null && !textScenario.getInstructions().isEmpty();
    }
    return false;
  }
}
