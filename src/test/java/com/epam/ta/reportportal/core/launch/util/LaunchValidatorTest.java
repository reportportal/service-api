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

package com.epam.ta.reportportal.core.launch.util;

import static com.epam.reportportal.rules.exception.ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class LaunchValidatorTest {

  @Test
  void validate() {
    Launch launch = new Launch();
    launch.setStatus(StatusEnum.IN_PROGRESS);
    launch.setStartTime(Instant.ofEpochMilli(1575551458336L));

    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.ofEpochMilli(1575551458334L));

    ReportPortalException reportPortalException = assertThrows(ReportPortalException.class,
        () -> LaunchValidator.validate(launch, finishExecutionRQ)
    );

    assertEquals(
        Suppliers.formattedSupplier(FINISH_TIME_EARLIER_THAN_START_TIME.getDescription(),
        finishExecutionRQ.getEndTime(),
        launch.getStartTime(),
        launch.getId()
    ).get(), reportPortalException.getMessage());
  }

}
