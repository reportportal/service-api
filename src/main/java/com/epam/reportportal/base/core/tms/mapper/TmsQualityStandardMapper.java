package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRS;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandard;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandardCriterion;
import java.util.List;
import java.util.function.Function;
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
   * Builds the criteria list for a (re)saved standard, upserting by {@code id}:
   * an existing criterion whose id is present in {@code criteriaRQ} is mutated in
   * place (so Hibernate updates it instead of deleting+recreating it under
   * {@code orphanRemoval}); a criterion with no id, or whose id isn't found, is
   * created new. A criterion whose id is absent from {@code criteriaRQ} is left
   * out of the result, which is what removes it via {@code orphanRemoval}.
   */
  public List<TmsQualityStandardCriterion> mergeCriteria(TmsQualityStandard standard,
      List<TmsQualityStandardCriterionRQ> criteriaRQ) {
    var existingById = (standard.getCriteria() == null ? List.<TmsQualityStandardCriterion>of() : standard.getCriteria())
        .stream()
        .filter(criterion -> criterion.getId() != null)
        .collect(Collectors.toMap(TmsQualityStandardCriterion::getId, Function.identity()));

    return criteriaRQ.stream()
        .map(rq -> {
          var criterion = rq.getId() == null ? null : existingById.get(rq.getId());
          if (criterion == null) {
            criterion = new TmsQualityStandardCriterion();
            criterion.setStandard(standard);
          }
          criterion.setName(rq.getName());
          criterion.setMaxPoints(rq.getMaxPoints());
          criterion.setSequence(rq.getSequence());
          return criterion;
        })
        .collect(Collectors.toList());
  }
}
