package com.epam.reportportal.core.item;

import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.TEST_ITEM_NOT_FOUND;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
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
   * Creates TestItem for TMS test case in TO_RUN status.
   *
   * @param testCase test case entity
   * @param launch   launch entity
   * @return created test item
   */
  @Transactional
  public TestItem createToRunTestItemForTestCase(TmsTestCaseRS testCase, Launch launch) {
    log.debug("Creating TO_RUN test item for test case: {} in launch: {}",
        testCase.getId(), launch.getId());

    // Create TestItem
    TestItem testItem = new TestItem();
    testItem.setUuid(UUID.randomUUID().toString());
    testItem.setName(testCase.getName());
    testItem.setDescription(testCase.getDescription());
    testItem.setType(TestItemTypeEnum.TEST);
    testItem.setLaunchId(launch.getId());
    testItem.setHasStats(true);
    testItem.setHasChildren(false);
    testItem.setRetryOf(null);
    testItem.setPath(String.valueOf(launch.getId()));

    // Create TestItemResults with TO_RUN status
    TestItemResults testItemResults = new TestItemResults();
    testItemResults.setStatus(StatusEnum.TO_RUN);
    testItemResults.setEndTime(null);
    testItemResults.setDuration(null);

    // Link TestItem and TestItemResults
    testItem.setItemResults(testItemResults);
    testItemResults.setTestItem(testItem);

    // Save (cascade will save TestItemResults)
    testItem = testItemRepository.save(testItem);

    log.info("Created TO_RUN test item: {} for test case: {}",
        testItem.getItemId(), testCase.getId());

    return testItem;
  }

  /**
   * Deletes test item by ID.
   *
   * @param itemId test item ID
   */
  @Transactional
  public void deleteTestItem(Long itemId) {
    log.debug("Deleting test item: {}", itemId);

    TestItem testItem = testItemRepository.findById(itemId)
        .orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, itemId));

    testItemRepository.delete(testItem);

    log.info("Deleted test item: {}", itemId);
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

  @Transactional
  public TestItem patchTestItemStatus(TestItem testItem, String status) {
    testItem.getItemResults().setStatus(StatusEnum.valueOf(status));
    return testItemRepository.save(testItem);
  }
}
