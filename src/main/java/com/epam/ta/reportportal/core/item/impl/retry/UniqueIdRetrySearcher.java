package com.epam.ta.reportportal.core.item.impl.retry;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("uniqueIdRetrySearcher")
public class UniqueIdRetrySearcher implements RetrySearcher {

  private final UniqueIdGenerator uniqueIdGenerator;
  private final TestItemRepository testItemRepository;

  public UniqueIdRetrySearcher(UniqueIdGenerator uniqueIdGenerator,
      TestItemRepository testItemRepository) {
    this.uniqueIdGenerator = uniqueIdGenerator;
    this.testItemRepository = testItemRepository;
  }

  @Override
  public Optional<Long> findPreviousRetry(Launch launch, TestItem newItem, TestItem parentItem) {
    if (Objects.isNull(newItem.getUniqueId())) {
      newItem.setUniqueId(
          uniqueIdGenerator.generate(newItem, IdentityUtil.getItemTreeIds(parentItem), launch));
    }
    return ofNullable(newItem.getItemId()).map(
            itemId -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentIdAndItemIdNotEqual(
                newItem.getUniqueId(),
                launch.getId(),
                parentItem.getItemId(),
                itemId
            ))
        .orElseGet(() -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentId(
            newItem.getUniqueId(),
            launch.getId(),
            parentItem.getItemId()
        ));
  }
}
