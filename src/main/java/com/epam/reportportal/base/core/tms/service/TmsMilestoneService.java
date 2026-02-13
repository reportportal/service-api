package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsMilestoneRS;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneRQ;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneRS;
import com.epam.reportportal.base.model.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing milestones in the TMS module.
 */
public interface TmsMilestoneService {

  /**
   * Creates a new milestone.
   *
   * @param projectId  the project ID
   * @param milestoneRQ the milestone request data
   * @return created milestone details
   */
  TmsMilestoneRS create(Long projectId, TmsMilestoneRQ milestoneRQ);

  /**
   * Retrieves a milestone by its ID.
   *
   * @param projectId   the project ID
   * @param milestoneId the milestone ID
   * @return milestone details
   */
  TmsMilestoneRS getById(Long projectId, Long milestoneId);

  /**
   * Retrieves milestones by criteria with pagination.
   *
   * @param projectId     the project ID
   * @param pageable pagination details
   * @return paginated list of milestones
   */
  Page<TmsMilestoneRS> getAll(Long projectId, Pageable pageable);

  /**
   * Applies partial updates to an existing milestone.
   *
   * @param projectId   the project ID
   * @param milestoneId the milestone ID
   * @param milestoneRQ the milestone patch data
   * @return updated milestone details
   */
  TmsMilestoneRS patch(Long projectId, Long milestoneId, TmsMilestoneRQ milestoneRQ);

  /**
   * Deletes a milestone by its ID.
   *
   * @param projectId   the project ID
   * @param milestoneId the milestone ID
   */
  void delete(Long projectId, Long milestoneId);

  /**
   * Removes a test plan from a milestone.
   *
   * @param projectId   the project ID
   * @param milestoneId the milestone ID
   * @param testPlanId  the test plan ID to remove
   */
  void removeTestPlanFromMilestone(Long projectId, Long milestoneId, Long testPlanId);

  /**
   * Added a test plan to a milestone.
   *
   * @param projectId   the project ID
   * @param milestoneId the milestone ID
   * @param testPlanId  the test plan ID to add
   */
  void addTestPlanToMilestone(Long projectId, Long milestoneId, Long testPlanId);

  /**
   * Duplicates an existing milestone.
   *
   * @param projectId            the project ID
   * @param milestoneId          the milestone ID to duplicate
   * @param duplicateMilestoneRQ the duplicate milestone request data
   * @return duplicated milestone details
   */
  DuplicateTmsMilestoneRS duplicate(Long projectId, Long milestoneId, TmsMilestoneRQ duplicateMilestoneRQ);
}
