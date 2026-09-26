package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;
import com.epam.reportportal.base.core.tms.mapper.TmsQualityStandardMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsQualityStandardRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandard;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsQualityStandardServiceImpl implements TmsQualityStandardService {

  private final TmsQualityStandardRepository tmsQualityStandardRepository;
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
    tmsQualityStandardRepository.deleteByProjectId(projectId);
  }

  private void applyRQ(TmsQualityStandard standard, TmsQualityStandardRQ rq) {
    standard.setName(rq.getName());
    standard.setDescription(rq.getDescription());
    var criteria = tmsQualityStandardMapper.toCriteria(rq.getCriteria());
    criteria.forEach(c -> c.setStandard(standard));
    if (standard.getCriteria() == null) {
      standard.setCriteria(criteria);
    } else {
      standard.getCriteria().clear();
      standard.getCriteria().addAll(criteria);
    }
  }

  private TmsQualityStandard getOrThrow(Long projectId) {
    return tmsQualityStandardRepository.findByProjectId(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Quality standard for project '" + projectId + "'"));
  }
}
