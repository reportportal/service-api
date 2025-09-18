package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenarioPreconditionsAttachment;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenarioPreconditionsAttachmentId;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TmsManualScenarioPreconditionsAttachment junction entities.
 */
@Repository
public interface TmsManualScenarioPreconditionsAttachmentRepository extends
    ReportPortalRepository<TmsManualScenarioPreconditionsAttachment, TmsManualScenarioPreconditionsAttachmentId> {

  /**
   * Finds all preconditions-attachment relationships by preconditions ID.
   */
  List<TmsManualScenarioPreconditionsAttachment> findByIdPreconditionsId(Long preconditionsId);

  /**
   * Finds all preconditions-attachment relationships by attachment ID.
   */
  List<TmsManualScenarioPreconditionsAttachment> findByIdAttachmentId(Long attachmentId);

  /**
   * Finds all preconditions-attachment relationships by attachment IDs.
   */
  List<TmsManualScenarioPreconditionsAttachment> findByIdAttachmentIdIn(
      Collection<Long> attachmentIds);

  /**
   * Finds all preconditions-attachment relationships by preconditions IDs.
   */
  List<TmsManualScenarioPreconditionsAttachment> findByIdPreconditionsIdIn(
      Collection<Long> preconditionsIds);

  /**
   * Deletes all relationships by preconditions ID.
   */
  @Modifying
  @Query("DELETE FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId = :preconditionsId")
  void deleteByPreconditionsId(@Param("preconditionsId") Long preconditionsId);

  /**
   * Deletes all relationships by preconditions IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN :preconditionsIds")
  void deleteByPreconditionsIdIn(@Param("preconditionsIds") Collection<Long> preconditionsIds);

  /**
   * Deletes all relationships by attachment ID.
   */
  @Modifying
  @Query("DELETE FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.attachmentId = :attachmentId")
  void deleteByAttachmentId(@Param("attachmentId") Long attachmentId);

  /**
   * Deletes all relationships by attachment IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.attachmentId IN :attachmentIds")
  void deleteByAttachmentIdIn(@Param("attachmentIds") Collection<Long> attachmentIds);

  /**
   * Checks if relationship exists.
   */
  boolean existsById_PreconditionsIdAndId_AttachmentId(
      @Param("preconditionsId") Long preconditionsId,
      @Param("attachmentId") Long attachmentId);

  /**
   * Counts relationships by preconditions ID.
   */
  @Query("SELECT COUNT(mspa) FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId = :preconditionsId")
  long countByPreconditionsId(@Param("preconditionsId") Long preconditionsId);

  /**
   * Counts relationships by attachment ID.
   */
  @Query("SELECT COUNT(mspa) FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.attachmentId = :attachmentId")
  long countByAttachmentId(@Param("attachmentId") Long attachmentId);

  /**
   * Deletes all relationships for preconditions by test case ID.
   */
  @Modifying
  @Query(
      "DELETE FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN ("
          + "SELECT msp.id FROM TmsManualScenarioPreconditions msp WHERE msp.manualScenario.id IN ("
          + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
          + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId)))")
  void deleteByTestCaseId(@Param("testCaseId") Long testCaseId);

  /**
   * Deletes all relationships for preconditions by test case IDs.
   */
  @Modifying
  @Query(
      "DELETE FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN ("
          + "SELECT msp.id FROM TmsManualScenarioPreconditions msp WHERE msp.manualScenario.id IN ("
          + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
          + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id IN (:testCaseIds))))")
  void deleteByTestCaseIds(@Param("testCaseIds") Collection<Long> testCaseIds);

  /**
   * Deletes all relationships for preconditions in folder hierarchy.
   */
  @Modifying
  @Query(value = "DELETE FROM tms_manual_scenario_preconditions_attachment mspa "
      + "WHERE mspa.preconditions_id IN ("
      + "  SELECT msp.id FROM tms_manual_scenario_preconditions msp "
      + "  WHERE msp.manual_scenario_id IN ("
      + "    SELECT ms.id FROM tms_manual_scenario ms "
      + "    WHERE ms.test_case_version_id IN ("
      + "      SELECT tcv.id FROM tms_test_case_version tcv "
      + "      WHERE tcv.test_case_id IN ("
      + "        SELECT tc.id FROM tms_test_case tc "
      + "        WHERE tc.test_folder_id IN ("
      + "          WITH RECURSIVE folder_hierarchy AS ("
      + "            SELECT tf.id FROM tms_test_folder tf "
      + "            JOIN project p ON tf.project_id = p.id "
      + "            WHERE tf.id = :folderId AND p.id = :projectId "
      + "            UNION ALL "
      + "            SELECT tf.id FROM tms_test_folder tf "
      + "            JOIN folder_hierarchy fh ON tf.parent_id = fh.id "
      + "          ) "
      + "          SELECT id FROM folder_hierarchy "
      + "        ) "
      + "      ) "
      + "    ) "
      + "  ) "
      + ") ",
      nativeQuery = true)
  void deleteByTestFolderId(@Param("projectId") Long projectId, @Param("folderId") Long folderId);

  /**
   * Finds all relationships for preconditions by test case ID.
   */
  @Query(
      "SELECT mspa FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN ("
          + "SELECT msp.id FROM TmsManualScenarioPreconditions msp WHERE msp.manualScenario.id IN ("
          + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
          + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId)))")
  List<TmsManualScenarioPreconditionsAttachment> findByTestCaseId(
      @Param("testCaseId") Long testCaseId);

  /**
   * Finds all relationships for preconditions by test case IDs.
   */
  @Query(
      "SELECT mspa FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN ("
          + "SELECT msp.id FROM TmsManualScenarioPreconditions msp WHERE msp.manualScenario.id IN ("
          + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
          + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id IN (:testCaseIds))))")
  List<TmsManualScenarioPreconditionsAttachment> findByTestCaseIds(
      @Param("testCaseIds") Collection<Long> testCaseIds);

  /**
   * Finds all relationships for preconditions in folder hierarchy.
   */
  @Query(value = "SELECT mspa.* FROM tms_manual_scenario_preconditions_attachment mspa "
      + "WHERE mspa.preconditions_id IN ("
      + "  SELECT msp.id FROM tms_manual_scenario_preconditions msp "
      + "  WHERE msp.manual_scenario_id IN ("
      + "    SELECT ms.id FROM tms_manual_scenario ms "
      + "    WHERE ms.test_case_version_id IN ("
      + "      SELECT tcv.id FROM tms_test_case_version tcv "
      + "      WHERE tcv.test_case_id IN ("
      + "        SELECT tc.id FROM tms_test_case tc "
      + "        WHERE tc.test_folder_id IN ("
      + "          WITH RECURSIVE folder_hierarchy AS ("
      + "            SELECT tf.id FROM tms_test_folder tf "
      + "            JOIN project p ON tf.project_id = p.id "
      + "            WHERE tf.id = :folderId AND p.id = :projectId "
      + "            UNION ALL "
      + "            SELECT tf.id FROM tms_test_folder tf "
      + "            JOIN folder_hierarchy fh ON tf.parent_id = fh.id "
      + "          ) "
      + "          SELECT id FROM folder_hierarchy "
      + "        ) "
      + "      ) "
      + "    ) "
      + "  ) "
      + ") ",
      nativeQuery = true)
  List<TmsManualScenarioPreconditionsAttachment> findByTestFolderId(
      @Param("projectId") Long projectId, @Param("folderId") Long folderId);

  /**
   * Checks if any relationships exist for preconditions by test case ID.
   */
  @Query(
      "SELECT COUNT(mspa) > 0 FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN ("
          + "SELECT msp.id FROM TmsManualScenarioPreconditions msp WHERE msp.manualScenario.id IN ("
          + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
          + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId)))")
  boolean existsByTestCaseId(@Param("testCaseId") Long testCaseId);

  /**
   * Counts relationships for preconditions by test case ID.
   */
  @Query(
      "SELECT COUNT(mspa) FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN ("
          + "SELECT msp.id FROM TmsManualScenarioPreconditions msp WHERE msp.manualScenario.id IN ("
          + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
          + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId)))")
  long countByTestCaseId(@Param("testCaseId") Long testCaseId);

  /**
   * Counts relationships for preconditions by test case IDs.
   */
  @Query(
      "SELECT COUNT(mspa) FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id.preconditionsId IN ("
          + "SELECT msp.id FROM TmsManualScenarioPreconditions msp WHERE msp.manualScenario.id IN ("
          + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
          + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id IN (:testCaseIds))))")
  long countByTestCaseIds(@Param("testCaseIds") Collection<Long> testCaseIds);

  /**
   * Batch delete multiple specific relationships.
   */
  @Modifying
  @Query("DELETE FROM TmsManualScenarioPreconditionsAttachment mspa WHERE mspa.id IN :ids")
  void deleteByIds(@Param("ids") Collection<TmsManualScenarioPreconditionsAttachmentId> ids);

  /**
   * Batch save relationships for performance optimization.
   */
  @Modifying
  @Query(value =
      "INSERT INTO tms_manual_scenario_preconditions_attachment (preconditions_id, attachment_id, created_at) "
          + "VALUES (:preconditionsId, :attachmentId, :createdAt) "
          + "ON CONFLICT (preconditions_id, attachment_id) DO NOTHING",
      nativeQuery = true)
  void insertIgnoreDuplicate(@Param("preconditionsId") Long preconditionsId,
      @Param("attachmentId") Long attachmentId,
      @Param("createdAt") java.time.Instant createdAt);

  /**
   * Finds all attachment IDs that are referenced in manual scenario preconditions attachments. *
   * This is optimized to return only IDs instead of full entities. * * @return list of attachment
   * IDs used in preconditions attachments
   */
  @Query("SELECT mspa.id.attachmentId FROM TmsManualScenarioPreconditionsAttachment mspa")
  List<Long> findAllAttachmentIds();
}
