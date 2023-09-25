package com.epam.ta.reportportal.core.events.listener;

import com.epam.ta.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.ta.reportportal.core.events.subscriber.EventSubscriber;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

public class TestItemFinishedEventListener {

  private final List<EventSubscriber<TestItemFinishedEvent>> subscribers;

  public TestItemFinishedEventListener(List<EventSubscriber<TestItemFinishedEvent>> subscribers) {
    this.subscribers = subscribers;
  }

  @Async(value = "eventListenerExecutor")
  @TransactionalEventListener
  public void onApplicationEvent(TestItemFinishedEvent event) {
    subscribers.forEach(s -> s.handleEvent(event));
  }
}
