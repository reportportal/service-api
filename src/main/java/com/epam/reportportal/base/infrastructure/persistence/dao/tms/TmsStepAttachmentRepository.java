package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStepAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStepAttachmentId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TmsStepAttachment junction entities.
 */
@Repository
public interface TmsStepAttachmentRepository extends
    ReportPortalRepository<TmsStepAttachment, TmsStepAttachmentId> {

  /**
   * Finds all step-attachment relationships by step ID.
   */
  List<TmsStepAttachment> findByIdStepId(Long stepId);

  /**
   * Finds all step-attachment relationships by attachment ID.
   */
  List<TmsStepAttachment> findByIdAttachmentId(Long attachmentId);

  /**
   * Finds all step-attachment relationships by step IDs.
   */
  List<TmsStepAttachment> findByIdStepIdIn(Collection<Long> stepIds);

  /**
   * Finds all step-attachment relationships by attachment IDs.
   */
  List<TmsStepAttachment> findByIdAttachmentIdIn(Collection<Long> attachmentIds);

  /**
   * Deletes all relationships by step ID.
   */
  @Modifying
  @Query("DELETE FROM TmsStepAttachment sa WHERE sa.id.stepId = :stepId")
  void deleteByStepId(@Param("stepId") Long stepId);

  /**
   * Deletes all relationships by step IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsStepAttachment sa WHERE sa.id.stepId IN :stepIds")
  void deleteByStepIdIn(@Param("stepIds") Collection<Long> stepIds);

  /**
   * Deletes all relationships by attachment ID.
   */
  @Modifying
  @Query("DELETE FROM TmsStepAttachment sa WHERE sa.id.attachmentId = :attachmentId")
  void deleteByAttachmentId(@Param("attachmentId") Long attachmentId);

  /**
   * Deletes all relationships by attachment IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsStepAttachment sa WHERE sa.id.attachmentId IN :attachmentIds")
  void deleteByAttachmentIdIn(@Param("attachmentIds") Collection<Long> attachmentIds);

  /**
   * Checks if relationship exists.
   */
  @Query("SELECT COUNT(sa) > 0 FROM TmsStepAttachment sa WHERE sa.id.stepId = :stepId AND sa.id.attachmentId = :attachmentId")
  boolean existsByStepIdAndAttachmentId(@Param("stepId") Long stepId,
      @Param("attachmentId") Long attachmentId);

  /**
   * Deletes all relationships for steps by test case ID.
   */
  @Modifying
  @Query("DELETE FROM TmsStepAttachment sa WHERE sa.id.stepId IN ("
      + "SELECT s.id FROM TmsStep s WHERE s.stepsManualScenario.manualScenarioId IN ("
      + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
      + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId)))")
  void deleteByTestCaseId(@Param("testCaseId") Long testCaseId);

  /**
   * Deletes all relationships for steps by test case IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsStepAttachment sa WHERE sa.id.stepId IN ("
      + "SELECT s.id FROM TmsStep s WHERE s.stepsManualScenario.manualScenarioId IN ("
      + "SELECT ms.id FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
      + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id IN (:testCaseIds))))")
  void deleteByTestCaseIds(@Param("testCaseIds") Collection<Long> testCaseIds);

  /**
   * Deletes all relationships for steps in folder hierarchy.
   */
  @Modifying
  @Query(value = "DELETE FROM tms_step_attachment sa "
      + "WHERE sa.step_id IN ("
      + "  SELECT s.id FROM tms_step s "
      + "  WHERE s.steps_manual_scenario_id IN ("
      + "    SELECT sms.manual_scenario_id FROM tms_steps_manual_scenario sms "
      + "    WHERE sms.manual_scenario_id IN ("
      + "      SELECT ms.id FROM tms_manual_scenario ms "
      + "      WHERE ms.test_case_version_id IN ("
      + "        SELECT tcv.id FROM tms_test_case_version tcv "
      + "        WHERE tcv.test_case_id IN ("
      + "          SELECT tc.id FROM tms_test_case tc "
      + "          WHERE tc.test_folder_id IN ("
      + "            WITH RECURSIVE folder_hierarchy AS ("
      + "              SELECT tf.id FROM tms_test_folder tf "
      + "              JOIN project p ON tf.project_id = p.id "
      + "              WHERE tf.id = :folderId AND p.id = :projectId "
      + "              UNION ALL "
      + "              SELECT tf.id FROM tms_test_folder tf "
      + "              JOIN folder_hierarchy fh ON tf.parent_id = fh.id "
      + "            ) "
      + "            SELECT id FROM folder_hierarchy "
      + "          ) "
      + "        ) "
      + "      ) "
      + "    ) "
      + "  ) "
      + ") ",
      nativeQuery = true)
  void deleteByTestFolderId(@Param("projectId") Long projectId, @Param("folderId") Long folderId);

  /**
   * Finds all attachment IDs that are referenced in step attachments. * This is optimized to return
   * only IDs instead of full entities. * * @return list of attachment IDs used in step attachments
   */
  @Query("SELECT sa.id.attachmentId FROM TmsStepAttachment sa")
  List<Long> findAllAttachmentIds();
}
