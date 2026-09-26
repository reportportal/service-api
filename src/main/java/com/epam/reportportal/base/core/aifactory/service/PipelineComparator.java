package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.enums.AttributeDirection;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Builds the comparison of two iterations from what they carry, without knowing what they are about:
 * stages are matched by name, metrics are the numeric attributes, per-row detail comes from result
 * sections both iterations have, and "same scope" means equal identity attributes.
 */
@Component
public class PipelineComparator {

  static final String DIFFERENT_SCOPE_NOTE = "Different scope - compare trends, not individual rows";

  public PipelineCompareRS compare(PipelineIterationDetailRS baseline, PipelineIterationDetailRS candidate) {
    var sameScope = sameScope(baseline, candidate);
    return PipelineCompareRS.builder()
        .baseline(baseline)
        .candidate(candidate)
        .sameScope(sameScope)
        .note(sameScope ? null : DIFFERENT_SCOPE_NOTE)
        .stages(compareStages(baseline, candidate))
        .metrics(compareMetrics(baseline, candidate))
        .sections(sameScope ? compareSections(baseline, candidate) : List.of())
        .build();
  }

  private boolean sameScope(PipelineIterationDetailRS baseline, PipelineIterationDetailRS candidate) {
    var baselineScope = identity(baseline);
    var candidateScope = identity(candidate);
    return !baselineScope.isEmpty() && baselineScope.equals(candidateScope);
  }

  private Set<String> identity(PipelineIterationDetailRS iteration) {
    if (iteration.getAttributes() == null) {
      return Set.of();
    }
    return iteration.getAttributes().stream()
        .filter(PipelineAttribute::isIdentity)
        .map(attribute -> attribute.getKey() + "=" + attribute.getValue())
        .collect(Collectors.toSet());
  }

  private List<PipelineCompareRS.StageCompare> compareStages(PipelineIterationDetailRS baseline,
      PipelineIterationDetailRS candidate) {
    var baselineByName = topLevelSummaries(baseline);
    var candidateByName = topLevelSummaries(candidate);
    var names = new LinkedHashSet<String>(candidateByName.keySet());
    names.addAll(baselineByName.keySet());
    return names.stream()
        .map(name -> PipelineCompareRS.StageCompare.builder()
            .name(name)
            .baseline(baselineByName.get(name))
            .candidate(candidateByName.get(name))
            .build())
        .toList();
  }

  private Map<String, PipelineStageSummaryRS> topLevelSummaries(PipelineIterationDetailRS iteration) {
    var result = new LinkedHashMap<String, PipelineStageSummaryRS>();
    iteration.getStages().stream()
        .filter(stage -> stage.getParentStageId() == null)
        .forEach(stage -> result.putIfAbsent(stage.getName(), summaryOf(stage)));
    return result;
  }

  private PipelineStageSummaryRS summaryOf(PipelineStageRS stage) {
    return PipelineStageSummaryRS.builder()
        .id(stage.getId())
        .name(stage.getName())
        .sequence(stage.getSequence())
        .attempt(stage.getAttempt())
        .parentStageId(stage.getParentStageId())
        .status(stage.getStatus())
        .statusCategory(stage.getStatusCategory())
        .origin(stage.getOrigin())
        .startedAt(stage.getStartedAt())
        .finishedAt(stage.getFinishedAt())
        .durationMillis(stage.getDurationMillis())
        .attributes(stage.getAttributes())
        .message(stage.getMessage())
        .build();
  }

  private List<PipelineCompareRS.MetricCompare> compareMetrics(PipelineIterationDetailRS baseline,
      PipelineIterationDetailRS candidate) {
    var baselineByKey = numericAttributes(baseline);
    var candidateByKey = numericAttributes(candidate);
    var keys = new LinkedHashSet<String>(candidateByKey.keySet());
    keys.addAll(baselineByKey.keySet());
    var metrics = new ArrayList<PipelineCompareRS.MetricCompare>();
    for (var key : keys) {
      var before = baselineByKey.get(key);
      var after = candidateByKey.get(key);
      var template = after != null ? after : before;
      var beforeNumber = before == null ? null : PipelineAttributeSupport.parse(before.getValue());
      var afterNumber = after == null ? null : PipelineAttributeSupport.parse(after.getValue());
      BigDecimal delta = beforeNumber != null && afterNumber != null ? afterNumber.subtract(beforeNumber) : null;
      var direction = after != null && after.getDirection() != null ? after.getDirection()
          : before != null ? before.getDirection() : null;
      metrics.add(PipelineCompareRS.MetricCompare.builder()
          .key(key)
          .label(template.getLabel() != null ? template.getLabel() : key)
          .valueType(template.getValueType())
          .baseline(before == null ? null : before.getValue())
          .candidate(after == null ? null : after.getValue())
          .delta(delta == null ? null : delta.stripTrailingZeros().toPlainString())
          .verdict(verdict(delta, direction))
          .build());
    }
    return metrics;
  }

  private Map<String, PipelineAttribute> numericAttributes(PipelineIterationDetailRS iteration) {
    if (iteration.getAttributes() == null) {
      return Map.of();
    }
    return iteration.getAttributes().stream()
        .filter(attribute -> attribute.getValueType() != null && attribute.getValueType().isNumeric())
        .collect(Collectors.toMap(PipelineAttribute::getKey, Function.identity(), (a, b) -> b, LinkedHashMap::new));
  }

  PipelineCompareRS.Verdict verdict(BigDecimal delta, AttributeDirection direction) {
    if (delta == null || direction == null || direction == AttributeDirection.NONE) {
      return PipelineCompareRS.Verdict.NOT_APPLICABLE;
    }
    int sign = delta.signum();
    if (sign == 0) {
      return PipelineCompareRS.Verdict.SAME;
    }
    boolean increased = sign > 0;
    return increased == (direction == AttributeDirection.HIGHER_BETTER)
        ? PipelineCompareRS.Verdict.BETTER : PipelineCompareRS.Verdict.WORSE;
  }

  private List<PipelineCompareRS.SectionCompare> compareSections(PipelineIterationDetailRS baseline,
      PipelineIterationDetailRS candidate) {
    var baselineStages = byName(baseline);
    var result = new ArrayList<PipelineCompareRS.SectionCompare>();
    for (var candidateStage : candidate.getStages()) {
      var baselineStage = baselineStages.get(candidateStage.getName());
      if (baselineStage == null || candidateStage.getSections() == null || baselineStage.getSections() == null) {
        continue;
      }
      for (var candidateSection : candidateStage.getSections()) {
        baselineStage.getSections().stream()
            .filter(section -> Objects.equals(section.getKey(), candidateSection.getKey()))
            .findFirst()
            .ifPresent(baselineSection -> result.add(PipelineCompareRS.SectionCompare.builder()
                .stageName(candidateStage.getName())
                .sectionKey(candidateSection.getKey())
                .title(candidateSection.getTitle())
                .columns(candidateSection.getColumns())
                .rows(joinRows(baselineSection, candidateSection))
                .build()));
      }
    }
    return result;
  }

  private Map<String, PipelineStageRS> byName(PipelineIterationDetailRS iteration) {
    return iteration.getStages().stream()
        .collect(Collectors.toMap(PipelineStageRS::getName, Function.identity(), (a, b) -> a, LinkedHashMap::new));
  }

  private List<PipelineCompareRS.RowCompare> joinRows(PipelineSection baseline, PipelineSection candidate) {
    var baselineRows = keyedRows(baseline);
    var candidateRows = keyedRows(candidate);
    var keys = new LinkedHashSet<String>(candidateRows.keySet());
    keys.addAll(baselineRows.keySet());
    return keys.stream()
        .map(key -> PipelineCompareRS.RowCompare.builder()
            .key(key)
            .baseline(baselineRows.get(key))
            .candidate(candidateRows.get(key))
            .build())
        .toList();
  }

  private Map<String, PipelineSection.Row> keyedRows(PipelineSection section) {
    var rows = new LinkedHashMap<String, PipelineSection.Row>();
    if (section.getRows() != null) {
      section.getRows().stream()
          .filter(row -> row.getKey() != null)
          .forEach(row -> rows.putIfAbsent(row.getKey(), row));
    }
    return rows;
  }
}
