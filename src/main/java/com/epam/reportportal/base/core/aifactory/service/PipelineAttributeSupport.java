package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.enums.AttributeRollup;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Operations on typed attributes that do not depend on what the attributes mean: normalizing a reported
 * list, merging, deriving the list-search text and summing flagged stage attributes into the iteration.
 */
final class PipelineAttributeSupport {

  static final int MAX_ATTRIBUTES = 100;

  private PipelineAttributeSupport() {
  }

  /**
   * Applies defaults and keeps the last attribute reported for each key, in first-seen order.
   */
  static List<PipelineAttribute> normalize(List<PipelineAttribute> reported) {
    if (reported == null) {
      return List.of();
    }
    var byKey = new LinkedHashMap<String, PipelineAttribute>();
    for (var attribute : reported) {
      if (attribute == null || attribute.getKey() == null || attribute.getKey().isBlank()) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Attribute 'key' must not be blank");
      }
      var copy = PipelineAttribute.builder()
          .key(attribute.getKey().trim())
          .label(attribute.getLabel())
          .value(attribute.getValue())
          .type(attribute.getType() == null ? AttributeType.TAG : attribute.getType())
          .valueType(attribute.getValueType() == null ? AttributeValueType.TEXT : attribute.getValueType())
          .url(attribute.getUrl())
          .order(attribute.getOrder())
          .direction(attribute.getDirection())
          .rollup(attribute.getRollup())
          .identity(attribute.isIdentity())
          .build();
      byKey.put(copy.getKey(), copy);
    }
    if (byKey.size() > MAX_ATTRIBUTES) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "At most " + MAX_ATTRIBUTES + " attributes are allowed");
    }
    return List.copyOf(byKey.values());
  }

  static PipelineAttributes toColumn(List<PipelineAttribute> normalized) {
    return normalized.isEmpty() ? null : new PipelineAttributes(normalized);
  }

  /**
   * Existing attributes with {@code updates} added or replaced by key and {@code removeKeys} dropped.
   */
  static List<PipelineAttribute> merge(PipelineAttributes existing, List<PipelineAttribute> updates,
      Collection<String> removeKeys) {
    var byKey = new LinkedHashMap<String, PipelineAttribute>();
    if (existing != null) {
      existing.getItems().forEach(attribute -> byKey.put(attribute.getKey(), attribute));
    }
    if (removeKeys != null) {
      removeKeys.forEach(byKey::remove);
    }
    normalize(updates).forEach(attribute -> byKey.put(attribute.getKey(), attribute));
    if (byKey.size() > MAX_ATTRIBUTES) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "At most " + MAX_ATTRIBUTES + " attributes are allowed");
    }
    return List.copyOf(byKey.values());
  }

  /**
   * Lower-cased attribute values, space separated, for the case-insensitive list search.
   */
  static String searchText(List<PipelineAttribute> attributes) {
    var text = attributes.stream()
        .map(PipelineAttribute::getValue)
        .filter(Objects::nonNull)
        .filter(value -> !value.isBlank())
        .collect(Collectors.joining(" "))
        .toLowerCase(Locale.ROOT);
    return text.isEmpty() ? null : text;
  }

  /**
   * One lower-cased token per attribute (see {@link PipelineAttribute#indexToken}), for the exact
   * attribute filter.
   */
  static String attributeIndex(List<PipelineAttribute> attributes) {
    var index = attributes.stream()
        .map(attribute -> PipelineAttribute.indexToken(attribute.getKey(), attribute.getValue()))
        .collect(Collectors.joining());
    return index.isEmpty() ? null : index;
  }

  /**
   * Attributes the stages flagged with {@link AttributeRollup#SUM}, summed over the stages, that the
   * iteration does not already carry under the same key.
   */
  static List<PipelineAttribute> rollup(List<PipelineStage> stages, Set<String> reservedKeys) {
    var sums = new LinkedHashMap<String, BigDecimal>();
    var templates = new LinkedHashMap<String, PipelineAttribute>();
    for (var stage : stages) {
      if (stage.getAttributes() == null) {
        continue;
      }
      for (var attribute : stage.getAttributes().getItems()) {
        if (attribute.getRollup() != AttributeRollup.SUM || reservedKeys.contains(attribute.getKey())) {
          continue;
        }
        var number = parse(attribute.getValue());
        if (number == null) {
          continue;
        }
        sums.merge(attribute.getKey(), number, BigDecimal::add);
        templates.putIfAbsent(attribute.getKey(), attribute);
      }
    }
    return sums.entrySet().stream()
        .map(entry -> {
          var template = templates.get(entry.getKey());
          return PipelineAttribute.builder()
              .key(template.getKey())
              .label(template.getLabel())
              .value(entry.getValue().stripTrailingZeros().toPlainString())
              .type(AttributeType.MAIN)
              .valueType(template.getValueType())
              .direction(template.getDirection())
              .build();
        })
        .toList();
  }

  static BigDecimal parse(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
