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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.core.launch.impl.FinishLaunchApprovalStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ErrorRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Konstantin Antipin
 */
@ExtendWith(MockitoExtension.class)
class LaunchReporterApprovalConsumerTest {

	@Mock
	LaunchRepository launchRepository;

	@Mock
	private FinishLaunchApprovalStrategy approvalStrategy;

	@Mock
	private AmqpTemplate amqpTemplate;

	@InjectMocks
	private LaunchReporterApprovalConsumer launchReporterApprovalConsumer;

	@Test
	void onFinishLaunch() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(new Date());
		finishExecutionRQ.setDescription("description");
		String launchId = "1";

		when(launchRepository.findByUuid(launchId)).thenReturn(Optional.of(new Launch()));
		doThrow(new ReportPortalException(1, "message", new ErrorRS())).when(approvalStrategy).verifyNoInProgressItems(any(Launch.class));

		assertThrows(ReportPortalException.class,
				() -> launchReporterApprovalConsumer.onFinishLaunch(finishExecutionRQ,
						"user",
						"test_project",
						launchId,
						"http://example.com",
						null
				)
		);

		verify(launchRepository).findByUuid(eq(launchId));
		verify(approvalStrategy).verifyNoInProgressItems(any(Launch.class));
		verify(amqpTemplate, never()).convertAndSend(anyString(), any(Object.class), any(MessagePostProcessor.class));
	}

	@Test
	void onFinishLaunchSpooledToRetry() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(new Date());
		finishExecutionRQ.setDescription("description");
		String launchId = "1";

		when(launchRepository.findByUuid(launchId)).thenReturn(Optional.of(new Launch()));
		launchReporterApprovalConsumer.onFinishLaunch(finishExecutionRQ, "user", "test_project", launchId, "http://example.com", null);

		verify(launchRepository).findByUuid(eq(launchId));
		verify(approvalStrategy).verifyNoInProgressItems(any(Launch.class));
		verify(amqpTemplate).convertAndSend(anyString(), any(Object.class), any(MessagePostProcessor.class));
	}

}