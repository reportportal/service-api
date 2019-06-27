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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;

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

	@Autowired
	public LogReporterConsumer(LogRepository logRepository, LaunchRepository launchRepository, TestItemRepository testItemRepository,
			TestItemService testItemService, DataStoreService dataStoreService, CreateAttachmentHandler createAttachmentHandler) {
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.testItemService = testItemService;
		this.dataStoreService = dataStoreService;
		this.createAttachmentHandler = createAttachmentHandler;
	}

	@RabbitListener(queues = "#{ @logQueue.name }")
	public void onLogCreate(@Payload DeserializablePair<SaveLogRQ, BinaryDataMetaInfo> payload,
			@Header(MessageHeaders.PROJECT_ID) Long projectId, @Header(MessageHeaders.ITEM_ID) String itemId,
			@Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {

		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping log request TestItem {}, on maximum retry attempts {}", itemId, DEAD_LETTER_MAX_RETRY);
				cleanup(payload);
				return;
			}
			LOGGER.warn("Retrying log request TestItem {}, attempt {}", itemId, count);
		}

		SaveLogRQ request = payload.getLeft();
		BinaryDataMetaInfo metaInfo = payload.getRight();

		Optional<TestItem> itemOptional = testItemRepository.findByUuid(request.getItemId());

		if (itemOptional.isPresent()) {
			TestItem item = itemOptional.get();
			Log log = new LogBuilder().addSaveLogRq(request).addTestItem(item).get();
			logRepository.save(log);

			saveAttachment(metaInfo, log.getId(), projectId, testItemService.getEffectiveLaunch(item).getId(), item.getItemId());
		} else {
			Launch launch = launchRepository.findByUuid(request.getItemId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, request.getItemId()));

			Log log = new LogBuilder().addSaveLogRq(request).addLaunch(launch).get();
			logRepository.save(log);

			saveAttachment(metaInfo, log.getId(), projectId, launch.getId(), null);
		}
	}

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
			AttachmentBuilder attachmentBuilder = new AttachmentBuilder().withMetaInfo(metaInfo)
					.withProjectId(projectId)
					.withLaunchId(launchId);

			if (!Objects.isNull(itemId)) {
				attachmentBuilder.withItemId(itemId);
			}

			createAttachmentHandler.create(attachmentBuilder.get(), logId);
		}
	}
}
