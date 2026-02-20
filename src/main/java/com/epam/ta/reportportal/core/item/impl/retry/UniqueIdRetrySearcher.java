package com.epam.ta.reportportal.core.item.impl.retry;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.core.item.repository.TestItemLastModifiedRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Finds the previous try by matching {@code unique_id} within the same launch and tree parent.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RequiredArgsConstructor
@Service("uniqueIdRetrySearcher")
public class UniqueIdRetrySearcher implements RetrySearcher {

  private final UniqueIdGenerator uniqueIdGenerator;
  private final TestCaseHashGenerator testCaseHashGenerator;
  private final TestItemRepository testItemRepository;
  private final TestItemLastModifiedRepository testItemLastModifiedRepository;

  @Override
  public Optional<Long> findPreviousTry(Launch launch, TestItem retry) {
    if (Objects.isNull(retry.getUniqueId())) {
      retry.setUniqueId(
          uniqueIdGenerator.generate(retry, IdentityUtil.getParentIds(retry), launch));
    }
    return ofNullable(retry.getItemId()).map(
            itemId -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentIdAndItemIdNotEqual(
                retry.getUniqueId(),
                launch.getId(),
                retry.getParentId(),
                itemId
            ))
        .orElseGet(() -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentId(
            retry.getUniqueId(),
            launch.getId(),
            retry.getParentId()
        ));
  }
}
