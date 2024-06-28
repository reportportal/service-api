package com.epam.ta.reportportal.health;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
