/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.reporting.async.producer;

import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.DEFAULT_CONSISTENT_HASH_ROUTING_KEY;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_EXCHANGE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.config.RequestType;
import com.epam.ta.reportportal.ws.reporting.ItemCreatedRS;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ItemStartProducer implements StartTestItemHandler {

  private final AmqpTemplate amqpTemplate;

  public ItemStartProducer(@Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  private static void provideItemUuid(StartTestItemRQ request) {
    if (!StringUtils.hasText(request.getUuid())) {
      request.setUuid(UUID.randomUUID().toString());
    }
  }

  @Override
  public ItemCreatedRS startRootItem(ReportPortalUser user, MembershipDetails membershipDetails,
      StartTestItemRQ request) {
    provideItemUuid(request);
    amqpTemplate.convertAndSend(REPORTING_EXCHANGE,
        DEFAULT_CONSISTENT_HASH_ROUTING_KEY,
        request,
        message -> {
          Map<String, Object> headers = message.getMessageProperties().getHeaders();
          headers.put(MessageHeaders.HASH_ON,
              ofNullable(request.getLaunchUuid()).orElseThrow(() -> new ReportPortalException(
                  ErrorType.BAD_REQUEST_ERROR, "Launch UUID should not be null or empty.")));
          headers.put(MessageHeaders.REQUEST_TYPE, RequestType.START_TEST);
          headers.put(MessageHeaders.USERNAME, user.getUsername());
          headers.put(MessageHeaders.PROJECT_NAME, membershipDetails.getProjectName());
          headers.put(MessageHeaders.PARENT_ITEM_ID, "");
          return message;
        }
    );

    ItemCreatedRS response = new ItemCreatedRS();
    response.setId(request.getUuid());
    return response;
  }

  @Override
  public ItemCreatedRS startChildItem(ReportPortalUser user, MembershipDetails membershipDetails,
      StartTestItemRQ request, String parentId) {
    final String launchUuid = ofNullable(request.getLaunchUuid()).orElseThrow(
        () -> new ReportPortalException(
            ErrorType.BAD_REQUEST_ERROR, "Launch UUID should not be null or empty."));
    provideItemUuid(request);
    amqpTemplate.convertAndSend(
        REPORTING_EXCHANGE,
        DEFAULT_CONSISTENT_HASH_ROUTING_KEY,
        request,
        message -> {
          Map<String, Object> headers = message.getMessageProperties().getHeaders();
          headers.put(MessageHeaders.HASH_ON, launchUuid);
          headers.put(MessageHeaders.REQUEST_TYPE, RequestType.START_TEST);
          headers.put(MessageHeaders.USERNAME, user.getUsername());
          headers.put(MessageHeaders.PROJECT_NAME, membershipDetails.getProjectName());
          headers.put(MessageHeaders.PARENT_ITEM_ID, parentId);
          return message;
        }
    );

    ItemCreatedRS response = new ItemCreatedRS();
    response.setId(request.getUuid());
    return response;
  }
}
