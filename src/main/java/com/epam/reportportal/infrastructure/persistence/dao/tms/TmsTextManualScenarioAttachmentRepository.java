package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTextManualScenarioAttachment;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTextManualScenarioAttachmentId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TmsTextManualScenarioAttachment junction entities.
 */
@Repository
public interface TmsTextManualScenarioAttachmentRepository extends
    ReportPortalRepository<TmsTextManualScenarioAttachment, TmsTextManualScenarioAttachmentId> {

  /**
   * Finds all text manual scenario-attachment relationships by text manual scenario ID.
   */
  List<TmsTextManualScenarioAttachment> findByIdTextManualScenarioId(Long textManualScenarioId);

  /**
   * Finds all text manual scenario-attachment relationships by attachment ID.
   */
  List<TmsTextManualScenarioAttachment> findByIdAttachmentId(Long attachmentId);

  /**
   * Finds all text manual scenario-attachment relationships by attachment IDs.
   */
  List<TmsTextManualScenarioAttachment> findByIdAttachmentIdIn(Collection<Long> attachmentIds);

  /**
   * Deletes all relationships by text manual scenario ID.
   */
  @Modifying
  @Query("DELETE FROM TmsTextManualScenarioAttachment tmsa WHERE tmsa.id.textManualScenarioId = :textManualScenarioId")
  void deleteByTextManualScenarioId(@Param("textManualScenarioId") Long textManualScenarioId);

  /**
   * Deletes all relationships by attachment ID.
   */
  @Modifying
  @Query("DELETE FROM TmsTextManualScenarioAttachment tmsa WHERE tmsa.id.attachmentId = :attachmentId")
  void deleteByAttachmentId(@Param("attachmentId") Long attachmentId);

  /**
   * Deletes all relationships by attachment IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsTextManualScenarioAttachment tmsa WHERE tmsa.id.attachmentId IN :attachmentIds")
  void deleteByAttachmentIdIn(@Param("attachmentIds") Collection<Long> attachmentIds);

  /**
   * Checks if relationship exists.
   */
  @Query("SELECT COUNT(tmsa) > 0 FROM TmsTextManualScenarioAttachment tmsa WHERE tmsa.id.textManualScenarioId = :textManualScenarioId AND tmsa.id.attachmentId = :attachmentId")
  boolean existsByTextManualScenarioIdAndAttachmentId(
      @Param("textManualScenarioId") Long textManualScenarioId,
      @Param("attachmentId") Long attachmentId);

  /**
   * Deletes all relationships for text manual scenarios by test case ID.
   */
  @Modifying
  @Query("DELETE FROM TmsTextManualScenarioAttachment tmsa WHERE tmsa.id.textManualScenarioId IN ("
      + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
      + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId))")
  void deleteByTestCaseId(@Param("testCaseId") Long testCaseId);

  /**
   * Deletes all relationships for text manual scenarios by test case IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsTextManualScenarioAttachment tmsa WHERE tmsa.id.textManualScenarioId IN ("
      + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
      + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id IN (:testCaseIds)))")
  void deleteByTestCaseIds(@Param("testCaseIds") Collection<Long> testCaseIds);

  /**
   * Deletes all relationships for text manual scenarios in folder hierarchy.
   */
  @Modifying
  @Query(value = "DELETE FROM tms_text_manual_scenario_attachment tmsa "
      + "WHERE tmsa.text_manual_scenario_id IN ("
      + "  SELECT tms.manual_scenario_id FROM tms_text_manual_scenario tms "
      + "  WHERE tms.manual_scenario_id IN ("
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
   * Finds all attachment IDs that are referenced in text manual scenario attachments. * This is
   * optimized to return only IDs instead of full entities. * * @return list of attachment IDs used
   * in text manual scenario attachments
   */
  @Query("SELECT tmsa.id.attachmentId FROM TmsTextManualScenarioAttachment tmsa")
  List<Long> findAllAttachmentIds();
}
