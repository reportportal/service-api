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

package com.epam.ta.reportportal.core.analyzer.auto.client.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.rabbitmq.http.client.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RabbitMqManagementClientTemplateTest {

  @Mock
  private Client rabbitClient;

  private RabbitMqManagementClientTemplate template;

  @Test
  public void testReportPortalExceptionOnGetExchanges() {
    template = new RabbitMqManagementClientTemplate(rabbitClient, "analyzer");

    when(rabbitClient.getExchanges("analyzer")).thenReturn(null);

    assertThatThrownBy(() -> template.getAnalyzerExchangesInfo()).isInstanceOf(
        ReportPortalException.class);
  }
}