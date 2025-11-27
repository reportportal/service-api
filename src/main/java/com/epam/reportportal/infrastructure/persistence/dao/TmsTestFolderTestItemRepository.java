package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing TmsTestFolderTestItem junction entities.
 */
public interface TmsTestFolderTestItemRepository extends
    ReportPortalRepository<TmsTestFolderTestItem, Long> {

  @Query("SELECT tft FROM TmsTestFolderTestItem tft " +
      "WHERE tft.testFolderId = :testFolderId AND tft.testItem.itemId = :testItemId")
  Optional<TmsTestFolderTestItem> findByTestFolderIdAndTestItemId(
      @Param("testFolderId") Long testFolderId,
      @Param("testItemId") Long testItemId);

  @Query("SELECT COUNT(tft) > 0 FROM TmsTestFolderTestItem tft " +
      "WHERE tft.testFolderId = :testFolderId AND tft.testItem.itemId = :testItemId")
  boolean existsByTestFolderIdAndTestItemId(
      @Param("testFolderId") Long testFolderId,
      @Param("testItemId") Long testItemId);

  @Query("SELECT tft.testItem.itemId FROM TmsTestFolderTestItem tft " +
      "WHERE tft.testFolderId = :testFolderId AND tft.launchId = :launchId AND tft.testItem.parentId IS NULL")
  Optional<Long> findSuiteItemIdByTestFolderAndLaunch(
      @Param("testFolderId") Long testFolderId,
      @Param("launchId") Long launchId);

  /**
   * Finds all junction records for a given launch.
   *
   * @param launchId the Launch ID
   * @param pageable pagination
   * @return page of junction records
   */
  Page<TmsTestFolderTestItem> findByLaunchId(Long launchId, Pageable pageable);

  void deleteByTestItem_ItemId(Long testItemId);
}
