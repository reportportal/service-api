package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRS;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandard;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandardCriterion;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TmsQualityStandardMapper {

  public TmsQualityStandardRS toRS(TmsQualityStandard standard) {
    return TmsQualityStandardRS.builder()
        .id(standard.getId())
        .name(standard.getName())
        .description(standard.getDescription())
        .criteria(standard.getCriteria() == null ? List.of() : standard.getCriteria().stream()
            .map(this::toCriterionRS)
            .collect(Collectors.toList()))
        .createdAt(standard.getCreatedAt() == null ? null : standard.getCreatedAt().toEpochMilli())
        .updatedAt(standard.getUpdatedAt() == null ? null : standard.getUpdatedAt().toEpochMilli())
        .build();
  }

  private TmsQualityStandardCriterionRS toCriterionRS(TmsQualityStandardCriterion criterion) {
    return TmsQualityStandardCriterionRS.builder()
        .id(criterion.getId())
        .name(criterion.getName())
        .maxPoints(criterion.getMaxPoints())
        .sequence(criterion.getSequence())
        .build();
  }

  /**
   * Builds the criteria list for a (re)saved standard. The caller sets {@code
   * standard} on each returned criterion afterward (JPA back-reference).
   */
  public List<TmsQualityStandardCriterion> toCriteria(List<TmsQualityStandardCriterionRQ> criteriaRQ) {
    return criteriaRQ.stream()
        .map(rq -> {
          var criterion = new TmsQualityStandardCriterion();
          criterion.setName(rq.getName());
          criterion.setMaxPoints(rq.getMaxPoints());
          criterion.setSequence(rq.getSequence());
          return criterion;
        })
        .collect(Collectors.toList());
  }
}
