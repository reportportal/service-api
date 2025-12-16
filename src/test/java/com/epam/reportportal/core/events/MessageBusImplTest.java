package com.epam.reportportal.core.events;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

@ExtendWith(MockitoExtension.class)
public class MessageBusImplTest {

  @InjectMocks
  private MessageBusImpl messageBus;

  @Mock
  private AmqpTemplate amqpTemplate;

  private static final String EXCHANGE = "exchange";

  private static final String ROUTE = "route";

  private static final String MESSAGE = "message";

  @Test
  public void whenPublishWithExchange_thenCallConvertAndSend() {
    messageBus.publish(EXCHANGE, ROUTE, MESSAGE);

    verify(amqpTemplate).convertAndSend(EXCHANGE, ROUTE, MESSAGE);
  }

  @Test
  public void whenPublishWithoutExchange_thenCallConvertSendAndReceive() {
    messageBus.publish(ROUTE, MESSAGE);

    verify(amqpTemplate).convertAndSend(ROUTE, MESSAGE);
  }


}
