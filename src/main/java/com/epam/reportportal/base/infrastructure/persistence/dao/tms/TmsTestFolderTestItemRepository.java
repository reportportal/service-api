package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
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

  @Query("SELECT tft FROM TmsTestFolderTestItem tft WHERE tft.launchId = :launchId AND tft.testItem.itemId IN :testItemIds")
  List<TmsTestFolderTestItem> findAllByLaunchIdAndTestItemItemIdIn(
      @Param("launchId") Long launchId,
      @Param("testItemIds") java.util.Collection<Long> testItemIds);

  void deleteByTestItem_ItemId(Long testItemId);

  @Modifying
  @Query("DELETE FROM TmsTestFolderTestItem t WHERE t.launchId = :launchId")
  void deleteByLaunchId(@Param("launchId") Long launchId);
}
