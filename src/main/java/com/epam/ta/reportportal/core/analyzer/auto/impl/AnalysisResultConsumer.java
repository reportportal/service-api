/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.model.analyzer.AnalyzedItemRs;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens to the analyzer reply queue and delegates every incoming batch of {@link AnalyzedItemRs}
 * to {@link AnalysisResultHandler}. The analyzer instance name is read from the AMQP {@code app_id}
 * property when present.
 *
 * @author Pavel Bortnik
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisResultConsumer {

  private final AnalysisResultHandler analysisResultHandler;

  @RabbitListener(queues = "${rp.amqp.analyzerResponseQueue:analysis.matches}",
      containerFactory = "analyzerRabbitListenerContainerFactory")
  public void onReply(@Payload List<AnalyzedItemRs> analyzed,
      @Header(name = "analyzer_id", required = false) String analyzerInstance) {
    analysisResultHandler.processResults(analyzed, analyzerInstance);
  }
}
