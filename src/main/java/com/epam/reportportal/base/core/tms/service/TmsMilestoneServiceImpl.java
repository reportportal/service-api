package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsMilestoneRS;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneRQ;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneRS;
import com.epam.reportportal.base.core.tms.mapper.TmsMilestoneMapper;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsMilestoneFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsMilestoneServiceImpl implements TmsMilestoneService {

  private static final String MILESTONE_BY_ID =
      "Milestone with id: %d";

  private final TmsMilestoneMapper tmsMilestoneMapper;
  private final TmsMilestoneRepository tmsMilestoneRepository;
  private final TmsMilestoneFilterableRepository tmsMilestoneFilterableRepository;
  private final TmsTestPlanService tmsTestPlanService;

  @Override
  @Transactional
  public TmsMilestoneRS create(Long projectId, TmsMilestoneRQ milestoneRQ) {
    var milestone = tmsMilestoneMapper.toEntity(projectId, milestoneRQ);

    // ProductVersion is nullable and not required for now

    var savedMilestone = tmsMilestoneRepository.save(milestone);

    // New milestone doesn't have test plans yet
    return tmsMilestoneMapper.convert(savedMilestone, Collections.emptyList());
  }

  @Override
  @Transactional(readOnly = true)
  public TmsMilestoneRS getById(Long projectId, Long milestoneId) {
    var milestone = findMilestoneByIdAndProjectId(projectId, milestoneId);
    var testPlans = tmsTestPlanService.getByMilestoneId(projectId, milestoneId);

    return tmsMilestoneMapper.convert(milestone, testPlans);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsMilestoneRS> getAll(Long projectId, Filter filter, Pageable pageable) {
    var milestoneIdsPage = tmsMilestoneFilterableRepository.findIdsByProjectIdAndFilter(projectId,
        filter, pageable);

    if (milestoneIdsPage.isEmpty()) {
      return PagedResourcesAssembler
          .<TmsMilestoneRS>pageConverter()
          .apply(new PageImpl<>(Collections.emptyList(), pageable, 0));
    }

    var milestones = tmsMilestoneRepository.findAllById(milestoneIdsPage.getContent());

    var testPlansByMilestones = tmsTestPlanService.getByMilestoneIds(
        projectId,
        milestones
            .stream()
            .map(TmsMilestone::getId)
            .collect(Collectors.toList())
    );

    // Reorder milestones according to the paged IDs
    var milestoneMap = milestones
        .stream()
        .collect(Collectors.toMap(TmsMilestone::getId, Function.identity()));

    var milestonesRS = milestoneIdsPage
        .getContent()
        .stream()
        .map(milestoneMap::get)
        .filter(Objects::nonNull)
        .map(milestone -> {
          var testPlans = testPlansByMilestones.get(milestone.getId());
          return tmsMilestoneMapper.convert(milestone, testPlans);
        })
        .toList();

    return PagedResourcesAssembler
        .<TmsMilestoneRS>pageConverter()
        .apply(new PageImpl<>(
            milestonesRS,
            pageable,
            milestoneIdsPage.getTotalElements()
        ));
  }

  @Override
  @Transactional
  public TmsMilestoneRS patch(Long projectId, Long milestoneId, TmsMilestoneRQ milestoneRQ) {
    var milestone = findMilestoneByIdAndProjectId(projectId, milestoneId);

    tmsMilestoneMapper.patchEntity(projectId, milestoneRQ, milestone);

    var updatedMilestone = tmsMilestoneRepository.save(milestone);

    var testPlans = tmsTestPlanService.getByMilestoneId(projectId, milestoneId);

    return tmsMilestoneMapper.convert(updatedMilestone, testPlans);
  }

  @Override
  @Transactional
  public void delete(Long projectId, Long milestoneId) {
    if (!tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId)) {
      throw new ReportPortalException(
          ErrorType.NOT_FOUND, MILESTONE_BY_ID.formatted(milestoneId)
      );
    }

    tmsTestPlanService.removeTestPlansFromMilestone(projectId, milestoneId);

    var deletedCount = tmsMilestoneRepository.deleteByIdAndProjectId(milestoneId, projectId);

    if (deletedCount == 0) {
      throw new ReportPortalException(
          ErrorType.NOT_FOUND, MILESTONE_BY_ID.formatted(milestoneId)
      );
    }
  }

  @Override
  @Transactional
  public void removeTestPlanFromMilestone(Long projectId, Long milestoneId, Long testPlanId) {
    // Verify milestone exists
    if (!tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId)) {
      throw new ReportPortalException(ErrorType.NOT_FOUND,
          MILESTONE_BY_ID.formatted(milestoneId));
    }

    tmsTestPlanService.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId);
  }

  @Override
  @Transactional
  public void addTestPlanToMilestone(Long projectId, Long milestoneId, Long testPlanId) {
    // Verify milestone exists
    if (!tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId)) {
      throw new ReportPortalException(ErrorType.NOT_FOUND,
          MILESTONE_BY_ID.formatted(milestoneId));
    }

    tmsTestPlanService.addTestPlanMilestone(projectId, milestoneId, testPlanId);
  }

  @Override
  @Transactional
  public DuplicateTmsMilestoneRS duplicate(Long projectId, Long milestoneId,
      TmsMilestoneRQ duplicateMilestoneRQ) {
    // Verify an original milestone exists
    var originalMilestone = findMilestoneByIdAndProjectId(projectId, milestoneId);

    // Create a new milestone with data from request
    var newMilestone = tmsMilestoneMapper.toEntity(projectId, duplicateMilestoneRQ);

    var duplicateTestPlansRS = tmsTestPlanService.duplicateTestPlansInMilestone(
        projectId, milestoneId
    );

    var savedMilestone = tmsMilestoneRepository.save(newMilestone);

    return tmsMilestoneMapper.convertToDuplicateTmsMilestoneRS(
        savedMilestone, duplicateTestPlansRS
    );
  }

  /**
   * Finds a milestone by ID and project ID or throws exception.
   *
   * @param projectId   the project ID
   * @param milestoneId the milestone ID
   * @return found a milestone
   * @throws ReportPortalException if a milestone not found
   */
  private TmsMilestone findMilestoneByIdAndProjectId(Long projectId, Long milestoneId) {
    return tmsMilestoneRepository
        .findByIdAndProjectId(milestoneId, projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            MILESTONE_BY_ID.formatted(milestoneId)));
  }
}
