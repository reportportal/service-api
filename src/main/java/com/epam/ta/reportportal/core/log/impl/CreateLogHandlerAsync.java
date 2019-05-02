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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.configs.rabbit.DeserializablePair;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.ws.model.EntryCreatedAsyncRS;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.epam.ta.reportportal.ws.rabbit.MessageHeaders;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LOG;

/**
 * Asynchronous implementation of {@link ICreateLogHandler} using RabbitMQ
 * to defer binding Log to ID(s)
 *
 * @author Andrei Varabyeu
 */
@Service("asyncCreateLogHandler")
public class CreateLogHandlerAsync implements ICreateLogHandler {

	/**
	 * We are using {@link Provider} there because we need
	 * {@link SaveLogBinaryDataTaskAsync} with scope prototype. Since current class is in
	 * singleton scope, we have to find a way to get new instance of job for new
	 * execution
	 */
	@Autowired
	private Provider<SaveLogBinaryDataTaskAsync> saveLogBinaryDataTask;

	@Autowired
	@Qualifier("saveLogsTaskExecutor")
	private TaskExecutor taskExecutor;

	@Autowired
	@Qualifier(value = "rabbitTemplate")
	AmqpTemplate amqpTemplate;


	@Override
	@Nonnull
	public EntryCreatedAsyncRS createLog(@Nonnull SaveLogRQ request, MultipartFile file, ReportPortalUser.ProjectDetails projectDetails) {

		validate(request);

		request.setUuid(UUID.randomUUID().toString());

		if (file != null) {
			CompletableFuture.supplyAsync(saveLogBinaryDataTask.get()
					.withRequest(request)
					.withFile(file)
					.withProjectId(projectDetails.getProjectId()), taskExecutor
			).thenAccept(metaInfo -> {
					sendMessage(request, metaInfo, projectDetails.getProjectId());
			});
		} else {
			sendMessage(request, null, projectDetails.getProjectId());
		}

		EntryCreatedAsyncRS response = new EntryCreatedAsyncRS();
		response.setUuid(request.getUuid());
		return response;
	}

	private void sendMessage(SaveLogRQ request, BinaryDataMetaInfo metaInfo, Long projectId) {
		amqpTemplate.convertAndSend(QUEUE_LOG, DeserializablePair.of(request, metaInfo), message -> {
			Map<String, Object> headers = message.getMessageProperties().getHeaders();
			headers.put(MessageHeaders.PROJECT_ID, projectId);
			headers.put(MessageHeaders.ITEM_ID, request.getTestItemId());
			return message;
		});

	}
}
