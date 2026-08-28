/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.reporting.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.ShovelInfo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ReportingShovelServiceTest {

  @Test
  void convertsCommaSeparatedAddressesToShovelUriLists() {
    Client managementClient = mock(Client.class);
    ReportingShovelService shovelService = new ReportingShovelService(managementClient,
        "amqp://first, amqp://second/", "/reporting");

    shovelService.republishToReportingExchange("q.reporting.outdated.0");

    ArgumentCaptor<ShovelInfo> shovel = ArgumentCaptor.forClass(ShovelInfo.class);
    verify(managementClient).declareShovel(org.mockito.ArgumentMatchers.eq("/reporting"),
        shovel.capture());
    List<String> expectedUris =
        List.of("amqp://first/%2Freporting", "amqp://second/%2Freporting");
    assertEquals(expectedUris, shovel.getValue().getDetails().getSourceURIs());
    assertEquals(expectedUris, shovel.getValue().getDetails().getDestinationURIs());
  }
}
