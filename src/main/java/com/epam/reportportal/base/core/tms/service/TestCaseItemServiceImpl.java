package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.item.identity.IdentityUtil;
import com.epam.reportportal.base.core.item.identity.TestCaseHashGenerator;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.mapper.TestCaseItemBuilder;
import com.epam.reportportal.base.infrastructure.persistence.dao.ItemAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing TEST test items (test case executions in manual launches).
 * Handles creation of TEST items with attributes under SUITE items.
 *
 * @author ReportPortal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseItemServiceImpl implements TestCaseItemService {

  private final TestItemRepository testItemRepository;
  private final ItemAttributeRepository itemAttributeRepository;
  private final TestCaseItemBuilder testCaseItemBuilder;
  private final TestCaseHashGenerator testCaseHashGenerator;

  /**
   * Creates a TEST item (test case execution) under a SUITE item.
   * Includes attributes from the test case.
   *
   * @param testCase test case data
   * @param suiteItem parent SUITE item
   * @param launch launch entity
   * @return created TEST item (persisted)
   */
  @Transactional
  @Override
  public TestItem createTestCaseItem(
      TmsTestCaseRS testCase,
      TestItem suiteItem,
      Launch launch) {

    log.debug("Creating TEST item for test case: {} under SUITE item: {}",
        testCase.getName(), suiteItem.getItemId());

    // Build TEST item
    var testItem = testCaseItemBuilder.buildTestCaseItem(
        testCase, suiteItem, launch
    );

    testItem.setTestCaseHash(
        testCaseHashGenerator.generate(
            testItem,
            IdentityUtil.getItemTreeIds(suiteItem),
            launch.getProjectId()
        )
    );
    
    // Save test item first to get ID
    var savedTestItem = testItemRepository.save(testItem);
    
    // After first save we have the real ID
    var testResults = new TestItemResults();
    testResults.setStatus(StatusEnum.TO_RUN);
    testResults.setTestItem(savedTestItem);
    savedTestItem.setItemResults(testResults);
    
    // Now set path using generated ID
    savedTestItem.setPath(suiteItem.getPath() + "." + savedTestItem.getItemId());
    
    savedTestItem = testItemRepository.save(savedTestItem);
    
    // Process test case attributes if present
    if (CollectionUtils.isNotEmpty(testCase.getAttributes())) {
      log.debug("Creating {} item attributes for test case: {}",
          testCase.getAttributes().size(), testCase.getId());

      var itemAttributes = new ArrayList<ItemAttribute>();

      for (var testCaseAttribute : testCase.getAttributes()) {
        // Map according to requirements:
        // TmsTestCaseAttributeRS.key -> ItemAttribute.value
        // ItemAttribute.key = "tag"
        var itemAttribute = new ItemAttribute();
        itemAttribute.setKey("tag");
        itemAttribute.setValue(testCaseAttribute.getKey()); // key from TmsTestCaseAttributeRS becomes value
        itemAttribute.setSystem(false); // Test case attributes are non-system
        itemAttribute.setTestItem(savedTestItem);

        itemAttributes.add(itemAttribute);

        log.trace("Mapped test case attribute - key: '{}' -> ItemAttribute(key='tag', value='{}')",
            testCaseAttribute.getKey(), testCaseAttribute.getKey());
      }
      
      savedTestItem.getAttributes().addAll(itemAttributes);
      
      // Save all item attributes
      itemAttributeRepository.saveAll(itemAttributes);

      log.debug("Successfully saved {} item attributes for test case: {}",
          itemAttributes.size(), testCase.getId());
    }

    log.debug("Successfully created TEST item: {} for test case: {}",
        savedTestItem.getItemId(), testCase.getId());

    return savedTestItem;
  }

}
