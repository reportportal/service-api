package com.epam.reportportal.base.core.item;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.TEST_ITEM_NOT_FOUND;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test item list operations, counts, and attributes for the API layer.
 *
 * @author Konstantin Antipin
 */
@Slf4j
@Service
public class TestItemService {

  private final TestItemRepository testItemRepository;
  private final LaunchRepository launchRepository;

  @Autowired
  public TestItemService(TestItemRepository testItemRepository, LaunchRepository launchRepository) {
    this.testItemRepository = testItemRepository;
    this.launchRepository = launchRepository;
  }

  public Launch getEffectiveLaunch(TestItem testItem) {
    return ofNullable(testItem.getRetryOf()).map(retryParentId -> {
          TestItem retryParent = testItemRepository.findById(retryParentId)
              .orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND,
                  testItem.getRetryOf()));
          return getLaunch(retryParent);
        }).orElseGet(() -> getLaunch(testItem))
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND));
  }

  private Optional<Launch> getLaunch(TestItem testItem) {
    return ofNullable(testItem.getLaunchId()).map(launchRepository::findById)
        .orElseGet(() -> ofNullable(testItem.getParentId()).flatMap(testItemRepository::findById)
            .map(TestItem::getLaunchId)
            .map(launchRepository::findById)
            .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND)));
  }

  /**
   * Deletes all test items by launch ID.
   *
   * @param projectId
   * @param launchId  launch ID
   */
  @Transactional
  public void deleteByLaunchId(long projectId, Long launchId) {
    log.debug("Deleting all test items for launch: {}", launchId);

    // Verify launch exists
    if (!launchRepository.existsByIdAndProjectId(launchId, projectId)) {
      throw new ReportPortalException(LAUNCH_NOT_FOUND, launchId);
    }

    int deletedCount = testItemRepository.deleteByLaunchId(launchId);

    log.info("Deleted {} test items for launch: {}", deletedCount, launchId);
  }
}
