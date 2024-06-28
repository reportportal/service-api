/*
 * Copyright 2024 EPAM Systems
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
package com.epam.ta.reportportal.health;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Health Indicator for rabbitmq.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class RabbitHealthIndicator extends AbstractHealthIndicator {

  private final RabbitTemplate rabbitTemplate;

  public RabbitHealthIndicator(RabbitTemplate rabbitTemplate) {
    super("Rabbit health check failed");
    Assert.notNull(rabbitTemplate, "RabbitTemplate must not be null");
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    rabbitTemplate.execute(Channel::getConnection);
    builder.up();
  }
}
