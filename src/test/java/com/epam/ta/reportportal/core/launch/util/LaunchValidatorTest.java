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

import static com.epam.ta.reportportal.commons.EntityUtils.TO_UTC_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.ws.model.ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class LaunchValidatorTest {

  @Test
  void validate() {
    Launch launch = new Launch();
    launch.setStatus(StatusEnum.IN_PROGRESS);
    launch.setStartTime(
        LocalDateTime.ofInstant(Instant.ofEpochMilli(1575551458336L), ZoneOffset.UTC));

    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(
        LocalDateTime.ofInstant(Instant.ofEpochMilli(1575551458334L), ZoneId.systemDefault()));

    ReportPortalException reportPortalException = assertThrows(ReportPortalException.class,
        () -> LaunchValidator.validate(launch, finishExecutionRQ)
    );

    assertEquals(Suppliers.formattedSupplier(FINISH_TIME_EARLIER_THAN_START_TIME.getDescription(),
        TO_UTC_LOCAL_DATE_TIME.apply(finishExecutionRQ.getEndTime()),
        launch.getStartTime(),
        launch.getId()
    ).get(), reportPortalException.getMessage());
  }

}
