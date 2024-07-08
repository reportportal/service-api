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

package com.epam.ta.reportportal.reporting.async;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class UnbindQueuesOnShutdown implements DisposableBean {

  private final List<Binding> bindings;
  private final List<String> queues;
  private final AmqpAdmin amqpAdmin;

  public UnbindQueuesOnShutdown(@Qualifier("reportingBindings") List<Binding> bindings,
      @Qualifier("reportingQueues") List<Queue> queues, AmqpAdmin amqpAdmin) {
    this.bindings = bindings;
    this.queues = queues.stream().map(Queue::getName).collect(Collectors.toList());
    this.amqpAdmin = amqpAdmin;
  }

  @Override
  public void destroy() {
    bindings.forEach(amqpAdmin::removeBinding);
    queues.forEach(q -> amqpAdmin.deleteQueue(q, true, true));
  }
}
