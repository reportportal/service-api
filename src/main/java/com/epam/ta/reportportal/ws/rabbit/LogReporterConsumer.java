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

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.core.configs.rabbit.DeserializablePair;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.impl.CreateAttachmentHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.AttachmentBuilder;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LOG_DLQ_DROPPED;

/**
 * @author Pavel Bortnik
 */

@Component
public class LogReporterConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogReporterConsumer.class);

	private final LogRepository logRepository;
	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final TestItemService testItemService;
	private final DataStoreService dataStoreService;
	private final CreateAttachmentHandler createAttachmentHandler;


	private AmqpTemplate amqpTemplate;

	@Autowired
	public LogReporterConsumer(LogRepository logRepository, LaunchRepository launchRepository, TestItemRepository testItemRepository,
			TestItemService testItemService, DataStoreService dataStoreService, CreateAttachmentHandler createAttachmentHandler,
			 @Qualifier("rabbitTemplate") AmqpTemplate amqpTemplate) {
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.testItemService = testItemService;
		this.dataStoreService = dataStoreService;
		this.createAttachmentHandler = createAttachmentHandler;
		this.amqpTemplate = amqpTemplate;
	}

	@RabbitListener(queues = "#{ @logQueue.name }")
	@Transactional
	public void onLogCreate(@Payload DeserializablePair<SaveLogRQ, BinaryDataMetaInfo> payload,
			@Header(MessageHeaders.PROJECT_ID) Long projectId, @Header(MessageHeaders.ITEM_ID) String itemId,
			@Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {

		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping to {} log request for TestItem {}, on maximum retry attempts {}",
						QUEUE_LOG_DLQ_DROPPED,
						itemId,
						DEAD_LETTER_MAX_RETRY);

				// don't cleanup to not loose binary content of dropped DLQ message
				// cleanup(payload);

				amqpTemplate.convertAndSend(QUEUE_LOG_DLQ_DROPPED, payload, message -> {
					Map<String, Object> headers = message.getMessageProperties().getHeaders();
					headers.put(MessageHeaders.PROJECT_ID, projectId);
					headers.put(MessageHeaders.ITEM_ID, itemId);
					return message;
				});
				return;
			}
			LOGGER.trace("Retrying log request TestItem {}, attempt {}", itemId, count);
		}

		try {
			SaveLogRQ request = payload.getLeft();
			BinaryDataMetaInfo metaInfo = payload.getRight();

			Optional<TestItem> itemOptional = testItemRepository.findByUuid(request.getItemId());

			if (itemOptional.isPresent()) {
				createItemLog(request, itemOptional.get(), metaInfo, projectId);
			} else {
				Launch launch = launchRepository.findByUuid(request.getItemId())
						.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_OR_LAUNCH_NOT_FOUND, request.getItemId()));
				createLaunchLog(request, launch, metaInfo, projectId);
			}
		} catch (Exception e) {
			if (e instanceof ReportPortalException && e.getMessage().startsWith("Test Item ")) {
				LOGGER.debug("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			} else {
				LOGGER.error("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			}
			throw e;
		}
	}

	private void createItemLog(SaveLogRQ request, TestItem item, BinaryDataMetaInfo metaInfo, Long projectId) {
		Log log = new LogBuilder().addSaveLogRq(request).addTestItem(item).get();
		logRepository.save(log);
		saveAttachment(metaInfo, log.getId(), projectId, testItemService.getEffectiveLaunch(item).getId(), item.getItemId());
	}

	private void createLaunchLog(SaveLogRQ request, Launch launch, BinaryDataMetaInfo metaInfo, Long projectId) {
		Log log = new LogBuilder().addSaveLogRq(request).addLaunch(launch).get();
		logRepository.save(log);
		saveAttachment(metaInfo, log.getId(), projectId, launch.getId(), null);
	}

	/**
	 * Cleanup log content corresponding to log request, that was stored in DataStore
	 *
	 * Consider how appropriate it to use this method for dropped messages, that exceeded retry count
	 * and were routed into dropped DLQ
	 *
	 * @param payload
	 */
	private void cleanup(DeserializablePair<SaveLogRQ, BinaryDataMetaInfo> payload) {
		// we need to delete only binary data, log and attachment shouldn't be dirty created
		if (payload.getRight() != null) {
			BinaryDataMetaInfo metaInfo = payload.getRight();
			dataStoreService.delete(metaInfo.getFileId());
			dataStoreService.delete(metaInfo.getThumbnailFileId());
		}
	}

	private void saveAttachment(BinaryDataMetaInfo metaInfo, Long logId, Long projectId, Long launchId, Long itemId) {
		if (!Objects.isNull(metaInfo)) {
			Attachment attachment = new AttachmentBuilder().withMetaInfo(metaInfo)
					.withProjectId(projectId).withLaunchId(launchId).withItemId(itemId).get();

			createAttachmentHandler.create(attachment, logId);
		}
	}
}
