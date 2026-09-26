package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;
import com.epam.reportportal.base.core.tms.mapper.TmsQualityStandardMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsQualityStandardRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseQualityScoreRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandard;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandardCriterion;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsQualityStandardServiceImpl implements TmsQualityStandardService {

  private final TmsQualityStandardRepository tmsQualityStandardRepository;
  private final TmsTestCaseQualityScoreRepository tmsTestCaseQualityScoreRepository;
  private final TmsQualityStandardMapper tmsQualityStandardMapper;

  @Override
  @Transactional(readOnly = true)
  public TmsQualityStandardRS getStandard(Long projectId) {
    return tmsQualityStandardMapper.toRS(getOrThrow(projectId));
  }

  @Override
  @Transactional
  public TmsQualityStandardRS createStandard(Long projectId, TmsQualityStandardRQ rq) {
    if (tmsQualityStandardRepository.existsByProjectId(projectId)) {
      throw new ReportPortalException(ErrorType.RESOURCE_ALREADY_EXISTS,
          "Quality standard for project '" + projectId + "'");
    }
    var standard = new TmsQualityStandard();
    standard.setProjectId(projectId);
    applyRQ(standard, rq);
    return tmsQualityStandardMapper.toRS(tmsQualityStandardRepository.save(standard));
  }

  @Override
  @Transactional
  public TmsQualityStandardRS updateStandard(Long projectId, TmsQualityStandardRQ rq) {
    var standard = getOrThrow(projectId);
    applyRQ(standard, rq);
    return tmsQualityStandardMapper.toRS(tmsQualityStandardRepository.save(standard));
  }

  @Override
  @Transactional
  public void deleteStandard(Long projectId) {
    // criterion -> score is ON DELETE CASCADE, so deleting a standard that has scored test cases
    // would silently wipe their scoring history; same rule as removing a scored criterion on PUT.
    if (tmsTestCaseQualityScoreRepository.existsByCriterionStandardProjectId(projectId)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Cannot delete the quality standard: test cases already have scores against its criteria");
    }
    tmsQualityStandardRepository.deleteByProjectId(projectId);
  }

  private void applyRQ(TmsQualityStandard standard, TmsQualityStandardRQ rq) {
    standard.setName(rq.getName());
    standard.setDescription(rq.getDescription());
    guardAgainstDuplicateKeys(rq);
    guardAgainstRemovingScoredCriteria(standard, rq);
    var criteria = tmsQualityStandardMapper.mergeCriteria(standard, rq.getCriteria());
    if (standard.getCriteria() == null) {
      standard.setCriteria(criteria);
    } else {
      standard.getCriteria().clear();
      standard.getCriteria().addAll(criteria);
    }
  }

  private void guardAgainstDuplicateKeys(TmsQualityStandardRQ rq) {
    var duplicateKeys = rq.getCriteria().stream()
        .map(TmsQualityStandardCriterionRQ::getKey)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(key -> key, Collectors.counting()))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 1)
        .map(java.util.Map.Entry::getKey)
        .toList();
    if (!duplicateKeys.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Duplicate quality standard criterion keys: " + duplicateKeys);
    }
  }

  /**
   * Criteria dropped from the request are deleted via {@code orphanRemoval} once merged - refuse
   * that here for any criterion historical {@link com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseQualityScore}
   * rows still reference, since {@code tms_test_case_quality_score.criterion_id} cascades on
   * delete and would silently destroy that scoring history.
   */
  private void guardAgainstRemovingScoredCriteria(TmsQualityStandard standard, TmsQualityStandardRQ rq) {
    if (standard.getCriteria() == null || standard.getCriteria().isEmpty()) {
      return;
    }
    var keptIds = rq.getCriteria().stream()
        .map(TmsQualityStandardCriterionRQ::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    var removedIds = standard.getCriteria().stream()
        .map(TmsQualityStandardCriterion::getId)
        .filter(id -> !keptIds.contains(id))
        .collect(Collectors.toSet());
    if (removedIds.isEmpty()) {
      return;
    }
    var scoredIds = tmsTestCaseQualityScoreRepository.findDistinctCriterionIdsIn(removedIds);
    if (!scoredIds.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Cannot remove quality standard criteria that already have test-case scores: " + scoredIds);
    }
  }

  private TmsQualityStandard getOrThrow(Long projectId) {
    return tmsQualityStandardRepository.findByProjectId(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Quality standard for project '" + projectId + "'"));
  }
}
