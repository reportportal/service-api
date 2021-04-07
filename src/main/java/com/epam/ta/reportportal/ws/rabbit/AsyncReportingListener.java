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

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.configs.rabbit.DeserializablePair;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.logging.RabbitMessageLogging;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.*;

/**
 * @author Konstantin Antipin
 */
public class AsyncReportingListener implements MessageListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncReportingListener.class);

	@Autowired
	private MessageConverter messageConverter;

	@Autowired
	@Qualifier("rabbitTemplate")
	private AmqpTemplate amqpTemplate;

	@Autowired
	private StartLaunchHandler startLaunchHandler;

	@Autowired
	private FinishLaunchHandler finishLaunchHandler;

	@Autowired
	private StartTestItemHandler startTestItemHandler;

	@Autowired
	private FinishTestItemHandler finishTestItemHandler;

	@Autowired
	private DatabaseUserDetailsService userDetailsService;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private TestItemService testItemService;

	@Autowired
	private AttachmentBinaryDataService attachmentBinaryDataService;

	@Override
	@RabbitMessageLogging
	public void onMessage(Message message) {

		try {
			if (breakRetrying(message)) {
				return;
			}

			RequestType requestType = getRequestType(message);
			Map<String, Object> headers = message.getMessageProperties().getHeaders();

			switch (requestType) {
				case START_LAUNCH:
					onStartLaunch((StartLaunchRQ) messageConverter.fromMessage(message),
							(String) headers.get(MessageHeaders.USERNAME),
							(String) headers.get(MessageHeaders.PROJECT_NAME)
					);
					break;
				case FINISH_LAUNCH:
					onFinishLaunch((FinishExecutionRQ) messageConverter.fromMessage(message),
							(String) headers.get(MessageHeaders.USERNAME),
							(String) headers.get(MessageHeaders.PROJECT_NAME),
							(String) headers.get(MessageHeaders.LAUNCH_ID),
							(String) headers.get(MessageHeaders.BASE_URL)
					);
					break;
				case START_TEST:
					onStartItem((StartTestItemRQ) messageConverter.fromMessage(message),
							(String) headers.get(MessageHeaders.USERNAME),
							(String) headers.get(MessageHeaders.PROJECT_NAME),
							(String) headers.get(MessageHeaders.PARENT_ITEM_ID)
					);
					break;
				case FINISH_TEST:
					onFinishItem((FinishTestItemRQ) messageConverter.fromMessage(message),
							(String) headers.get(MessageHeaders.USERNAME),
							(String) headers.get(MessageHeaders.PROJECT_NAME),
							(String) headers.get(MessageHeaders.ITEM_ID)
					);
					break;
				case LOG:
					Jackson2JsonMessageConverter converter = (Jackson2JsonMessageConverter) messageConverter;
					onLogCreate((DeserializablePair) converter.fromMessage(message,
							new ParameterizedTypeReference<DeserializablePair<SaveLogRQ, BinaryDataMetaInfo>>() {
							}
					), (Long) headers.get(MessageHeaders.PROJECT_ID));
					break;
				default:
					LOGGER.error("Unknown message type");
					break;
			}
		} catch (Throwable e) {
			if (e instanceof ReportPortalException && e.getMessage().startsWith("Test Item ")) {
				LOGGER.debug("exception : {}, message : {},  cause : {}",
						e.getClass().getName(),
						e.getMessage(),
						e.getCause() != null ? e.getCause().getMessage() : ""
				);
			} else {
				LOGGER.error("exception : {}, message : {},  cause : {}",
						e.getClass().getName(),
						e.getMessage(),
						e.getCause() != null ? e.getCause().getMessage() : ""
				);
			}
			throw new AmqpRejectAndDontRequeueException(e);
		}

	}

	public void onStartLaunch(StartLaunchRQ rq, String username, String projectName) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		startLaunchHandler.startLaunch(user, ProjectExtractor.extractProjectDetails(user, projectName), rq);
	}

	public void onFinishLaunch(FinishExecutionRQ rq, String username, String projectName, String launchId, String baseUrl) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishLaunchHandler.finishLaunch(launchId, rq, ProjectExtractor.extractProjectDetails(user, projectName), user, baseUrl);
	}

	public void onStartItem(StartTestItemRQ rq, String username, String projectName, String parentId) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		ReportPortalUser.ProjectDetails projectDetails = ProjectExtractor.extractProjectDetails(user, normalizeId(projectName));
		if (!Strings.isNullOrEmpty(parentId)) {
			startTestItemHandler.startChildItem(user, projectDetails, rq, parentId);
		} else {
			startTestItemHandler.startRootItem(user, projectDetails, rq);
		}
	}

	public void onFinishItem(FinishTestItemRQ rq, String username, String projectName, String itemId) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishTestItemHandler.finishTestItem(user, ProjectExtractor.extractProjectDetails(user, normalizeId(projectName)), itemId, rq);
	}

	public void onLogCreate(DeserializablePair<SaveLogRQ, BinaryDataMetaInfo> payload, Long projectId) {
		SaveLogRQ request = payload.getLeft();
		BinaryDataMetaInfo metaInfo = payload.getRight();

		Optional<TestItem> itemOptional = testItemRepository.findByUuid(request.getItemUuid());

		if (StringUtils.isNotEmpty(payload.getLeft().getItemUuid()) && !itemOptional.isPresent()) {
			throw new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, payload.getLeft().getItemUuid());
		}

		if (itemOptional.isPresent()) {
			createItemLog(request, itemOptional.get(), metaInfo, projectId);
		} else {
			Launch launch = launchRepository.findByUuid(request.getLaunchUuid())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, request.getLaunchUuid()));
			createLaunchLog(request, launch, metaInfo, projectId);
		}
	}

	/**
	 * Process xdHeader of the message, breaking processing if maximum retry limit reached
	 *
	 * @param message
	 * @return -
	 */
	private boolean breakRetrying(Message message) {
		List<Map<String, ?>> xdHeader = (List<Map<String, ?>>) message.getMessageProperties().getHeaders().get(MessageHeaders.XD_HEADER);

		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping on maximum retry limit request of type = {}, for target id = {} ",
						getRequestType(message),
						getTargetId(message)
				);

				// log request : don't cleanup to not loose binary content of dropped DLQ message
				// cleanup(payload);

				amqpTemplate.send(EXCHANGE_REPORTING_RETRY, QUEUE_DLQ, message);
				return true;
			}
		}
		return false;
	}

	private String getTargetId(Message message) {
		try {
			switch (getRequestType(message)) {
				case START_LAUNCH:
					return ((StartLaunchRQ) messageConverter.fromMessage(message)).getUuid();
				case FINISH_LAUNCH:
					return (String) message.getMessageProperties().getHeaders().get(MessageHeaders.LAUNCH_ID);
				case START_TEST:
					return ((StartTestItemRQ) messageConverter.fromMessage(message)).getUuid();
				case FINISH_TEST:
					return (String) message.getMessageProperties().getHeaders().get(MessageHeaders.ITEM_ID);
				case LOG:
					Jackson2JsonMessageConverter converter = (Jackson2JsonMessageConverter) messageConverter;
					return ((SaveLogRQ) ((DeserializablePair) converter.fromMessage(message,
							new ParameterizedTypeReference<DeserializablePair<SaveLogRQ, BinaryDataMetaInfo>>() {
							}
					)).getLeft()).getUuid();
				default:
					return "";
			}
		} catch (Throwable e) {
			return "";
		}
	}

	private void createItemLog(SaveLogRQ request, TestItem item, BinaryDataMetaInfo metaInfo, Long projectId) {
		Log log = new LogBuilder().addSaveLogRq(request).addTestItem(item).addProjectId(projectId).get();
		logRepository.save(log);
		Launch effectiveLaunch = testItemService.getEffectiveLaunch(item);
		saveAttachment(metaInfo,
				log.getId(),
				projectId,
				effectiveLaunch.getId(),
				item.getItemId(),
				effectiveLaunch.getUuid(),
				log.getUuid()
		);
	}

	private void createLaunchLog(SaveLogRQ request, Launch launch, BinaryDataMetaInfo metaInfo, Long projectId) {
		Log log = new LogBuilder().addSaveLogRq(request).addLaunch(launch).addProjectId(projectId).get();
		logRepository.save(log);
		saveAttachment(metaInfo, log.getId(), projectId, launch.getId(), null, launch.getUuid(), log.getUuid());
	}

	private void saveAttachment(BinaryDataMetaInfo metaInfo, Long logId, Long projectId, Long launchId, Long itemId, String launchUuid,
			String logUuid) {
		if (!Objects.isNull(metaInfo)) {
			attachmentBinaryDataService.attachToLog(metaInfo,
					AttachmentMetaInfo.builder()
							.withProjectId(projectId)
							.withLaunchId(launchId)
							.withItemId(itemId)
							.withLogId(logId)
							.withLaunchUuid(launchUuid)
							.withLogUuid(logUuid)
							.withCreationDate(LocalDateTime.now(ZoneOffset.UTC))
							.build()
			);
		}
	}

	private RequestType getRequestType(Message message) {
		return RequestType.valueOf((String) message.getMessageProperties().getHeaders().get(MessageHeaders.REQUEST_TYPE));
	}

}
