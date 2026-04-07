package com.epam.ta.reportportal.core.launch.changes;

import com.epam.ta.reportportal.core.item.repository.TestItemLastModifiedRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LastModifiedHandler {

  private static final int BATCH_SIZE = 500;

  private final TestItemLastModifiedRepository testItemLastModifiedRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Long updateItemsLastModified(Long launchId, Long lastItemId) {
    List<Long> ids = testItemLastModifiedRepository
        .findNextLeafItemBatch(launchId, lastItemId, BATCH_SIZE);
    if (!ids.isEmpty()) {
      testItemLastModifiedRepository.updateLastModifiedByItemIds(ids);
      return ids.getLast();
    }
    return 0L;
  }
}
