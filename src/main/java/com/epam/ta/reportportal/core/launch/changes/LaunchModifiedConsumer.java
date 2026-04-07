package com.epam.ta.reportportal.core.launch.changes;

import static com.epam.ta.reportportal.core.configs.rabbit.UpdateTrackingConfiguration.LAUNCH_MODIFIED_QUEUE;

import com.epam.ta.reportportal.core.item.repository.TestItemLastModifiedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Exclusive consumer that receives a launch ID and performs cursor-style batched updates of
 * {@code test_item.last_modified}. Each batch of {@value TestItemLastModifiedRepository#BATCH_SIZE}
 * rows runs in its own transaction to keep lock durations short.
 */
@Component
@RequiredArgsConstructor
public class LaunchModifiedConsumer {

  private final LastModifiedHandler lastModifiedHandler;

  @RabbitListener(
      queues = LAUNCH_MODIFIED_QUEUE,
      containerFactory = "exclusiveLastModifiedContainerFactory"
  )
  public void onMessage(@Payload LaunchModifiedMessage message) {
    Long launchId = message.getLaunchId();
    long cursor = 0;
    do {
      cursor = lastModifiedHandler.updateItemsLastModified(launchId, cursor);
    } while (cursor != 0);
  }
}
