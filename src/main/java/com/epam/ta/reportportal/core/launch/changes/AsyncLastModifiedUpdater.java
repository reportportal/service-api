package com.epam.ta.reportportal.core.launch.changes;

import com.epam.ta.reportportal.core.item.repository.TestItemLastModifiedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thin async wrapper that runs the batch {@code test_item.last_modified} update in a separate
 * thread with its own transaction. Extracted as a separate Spring bean so that {@code @Async} is
 * honoured (avoids self-invocation through the proxy).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncLastModifiedUpdater {

  private final TestItemLastModifiedRepository testItemLastModifiedRepository;

  @Async("eventListenerExecutor")
  @Transactional
  public void updateLastModified(Long launchId) {
    log.info("Async update of test_item last_modified fields for launch {}", launchId);
    testItemLastModifiedRepository.updateLastModifiedByLaunchId(launchId);
  }
}
