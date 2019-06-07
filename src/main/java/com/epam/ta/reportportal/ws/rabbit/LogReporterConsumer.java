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
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;

/**
 * @author Pavel Bortnik
 */

@Component
@Transactional
public class LogReporterConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogReporterConsumer.class);

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private TestItemService testItemService;

	@Autowired
	private DataStoreService dataStoreService;

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
		Optional<Launch> launchOptional = launchRepository.findByUuid(request.getItemId());

		expect(itemOptional.isPresent() ^ launchOptional.isPresent(), Predicate.isEqual(true)).verify(ErrorType.TEST_ITEM_NOT_FOUND,
				request.getItemId()
		);

		LogBuilder logBuilder = new LogBuilder().addSaveLogRq(request);
		itemOptional.ifPresent(logBuilder::addTestItem);
		launchOptional.ifPresent(logBuilder::addLaunch);
		Log log = logBuilder.get();
		logRepository.save(log);

		// attachment
		if (metaInfo != null) {
			Long launchId = itemOptional.map(it -> testItemService.getEffectiveLaunch(it).getId())
					.orElseGet(() -> launchOptional.get().getId());

			Attachment attachment = new AttachmentBuilder().withFileId(metaInfo.getFileId())
					.withThumbnailId(metaInfo.getThumbnailFileId())
					.withContentType(metaInfo.getContentType())
					.withProjectId(projectId)
					.withLaunchId(launchId)
					.withItemId(itemOptional.map(TestItem::getItemId).orElse(null))
					.get();

			attachmentRepository.save(attachment);
			log.setAttachment(attachment);
		}

		logRepository.save(log);
	}

	private void cleanup(DeserializablePair<SaveLogRQ, BinaryDataMetaInfo> payload) {
		// we need to delete only binary data, log and attachment shouldn't be dirty created
		if (payload.getRight() != null) {
			BinaryDataMetaInfo metaInfo = payload.getRight();
			dataStoreService.delete(metaInfo.getFileId());
			dataStoreService.delete(metaInfo.getThumbnailFileId());
		}
	}
}
