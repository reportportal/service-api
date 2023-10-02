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

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.DEFAULT_CONSISTENT_HASH_ROUTING_KEY;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_EXCHANGE;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.config.RequestType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import java.util.Map;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ItemFinishProducer implements FinishTestItemHandler {

  private final AmqpTemplate amqpTemplate;

  public ItemFinishProducer(@Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  @Override
  public OperationCompletionRS finishTestItem(ReportPortalUser user, ProjectDetails projectDetails,
      String testItemId, FinishTestItemRQ request) {
    amqpTemplate.convertAndSend(REPORTING_EXCHANGE,
        DEFAULT_CONSISTENT_HASH_ROUTING_KEY,
        request,
        message -> {
          Map<String, Object> headers = message.getMessageProperties().getHeaders();
          headers.put(MessageHeaders.HASH_ON, request.getLaunchUuid());
          headers.put(MessageHeaders.REQUEST_TYPE, RequestType.FINISH_TEST);
          headers.put(MessageHeaders.USERNAME, user.getUsername());
          headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
          headers.put(MessageHeaders.ITEM_ID, testItemId);
          return message;
        }
    );
    return new OperationCompletionRS(formattedSupplier("Accepted finish request for test item ID = {}", testItemId).get());
  }
}
