package com.epam.ta.reportportal.core.events;

import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_ACTIVITY;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_ATTACHMENT;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_EVENTS;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.QUEUE_ATTACHMENT_DELETE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.EventObject;
import org.junit.jupiter.api.BeforeEach;
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

  private static final Long PROJECT_ID = 1L;

  private static final EventObject EVENT_OBJECT = EventObject.ITEM;

  private static final String EVENT_NAME = "event";

  private Activity activity;

  private String activityKey;

  @BeforeEach
  public void setUp() {
    activity = mock(Activity.class);
    lenient().when(activity.getProjectId()).thenReturn(PROJECT_ID);
    lenient().when(activity.getObjectType()).thenReturn(EVENT_OBJECT);
    lenient().when(activity.getEventName()).thenReturn(EVENT_NAME);

    activityKey = String.format("activity.%d.%s.%s", PROJECT_ID, EVENT_OBJECT, EVENT_NAME);
  }

  @Test
  public void whenPublishWithExchange_thenCallConvertAndSend() {
    messageBus.publish(EXCHANGE, ROUTE, MESSAGE);

    verify(amqpTemplate).convertAndSend(EXCHANGE, ROUTE, MESSAGE);
  }

  @Test
  public void whenPublishWithoutExchange_thenCallConvertSendAndReceive() {
    messageBus.publish(ROUTE, MESSAGE);

    verify(amqpTemplate).convertSendAndReceive(ROUTE, MESSAGE);
  }

  @Test
  public void whenBroadcastEvent_thenCallConvertAndSendWithExchangeEvents() {
    messageBus.broadcastEvent(MESSAGE);

    verify(amqpTemplate).convertAndSend(EXCHANGE_EVENTS, "", MESSAGE);
  }

  @Test
  public void whenPublishActivity_andActivityIsNull_thenDoNothing() {
    ActivityEvent activityEvent = mock(ActivityEvent.class);
    messageBus.publishActivity(activityEvent);

    verify(amqpTemplate, never()).convertAndSend(any());
  }

  @Test
  public void whenPublishActivity_andActivityIsNotNull_andNotSavedEvent_thenDoNothing() {
    ActivityEvent activityEvent = mock(ActivityEvent.class);
    when(activityEvent.toActivity()).thenReturn(activity);
    messageBus.publishActivity(activityEvent);

    verify(amqpTemplate, never()).convertAndSend(any());
  }

  @Test
  public void whenPublishActivity_andActivityIsNotNull_andSavedEvent_thenCallConvertAndSend() {
    ActivityEvent activityEvent = mock(ActivityEvent.class);
    when(activityEvent.toActivity()).thenReturn(activity);
    when(activityEvent.isSavedEvent()).thenReturn(true);
    messageBus.publishActivity(activityEvent);

    verify(amqpTemplate).convertAndSend(EXCHANGE_ACTIVITY, activityKey, activity);
  }

  @Test
  public void whenPublishDeleteAttachmentEvent_thenCallConvertAndSendWithAttachmentExchange() {
    DeleteAttachmentEvent deleteAttachmentEvent = mock(DeleteAttachmentEvent.class);
    messageBus.publishDeleteAttachmentEvent(deleteAttachmentEvent);

    verify(amqpTemplate).convertAndSend(
        EXCHANGE_ATTACHMENT, QUEUE_ATTACHMENT_DELETE, deleteAttachmentEvent);
  }
}
