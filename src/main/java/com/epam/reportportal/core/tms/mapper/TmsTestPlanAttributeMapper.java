package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsTestPlanAttributeRS;
import com.epam.reportportal.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TMS Test Plan attribute DTOs and entities.
 */
@Component
public class TmsTestPlanAttributeMapper {

  /**
   * Converts TmsTestPlanAttribute junction entity to response DTO.
   *
   * @param entity TmsTestPlanAttribute junction entity
   * @return TmsTestPlanAttributeRS response DTO
   */
  public TmsTestPlanAttributeRS toResponse(TmsTestPlanAttribute entity) {
    if (entity == null || entity.getItemAttribute() == null) {
      return null;
    }

    var itemAttribute = entity.getItemAttribute();
    return TmsTestPlanAttributeRS.builder()
        .id(itemAttribute.getId())
        .key(itemAttribute.getKey())
        .value(itemAttribute.getValue())
        .build();
  }

  /**
   * Converts collection of TmsTestPlanAttribute entities to list of response DTOs.
   *
   * @param entities collection of TmsTestPlanAttribute entities
   * @return list of TmsTestPlanAttributeRS response DTOs
   */
  public List<TmsTestPlanAttributeRS> toResponseList(Collection<TmsTestPlanAttribute> entities) {
    if (entities == null) {
      return List.of();
    }

    return entities.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Creates ItemAttribute entity from request DTO.
   * ItemAttribute will have null testItem and launch (standalone attribute).
   *
   * @param request TmsTestPlanAttributeRQ request DTO
   * @return ItemAttribute entity
   */
  public ItemAttribute toItemAttribute(TmsTestPlanAttributeRQ request) {
    if (request == null) {
      return null;
    }

    return new ItemAttribute(request.getKey(), request.getValue(), false);
  }

  /**
   * Creates TmsTestPlanAttribute junction entity.
   *
   * @param testPlan the test plan
   * @param itemAttribute the item attribute
   * @return TmsTestPlanAttribute junction entity
   */
  public TmsTestPlanAttribute toTmsTestPlanAttribute(TmsTestPlan testPlan, ItemAttribute itemAttribute) {
    return new TmsTestPlanAttribute(testPlan, itemAttribute);
  }

  public TmsTestPlanAttribute duplicateTestPlanAttribute(TmsTestPlanAttribute originalAttr,
      TmsTestPlan newTestPlan) {
    if (originalAttr == null || originalAttr.getItemAttribute() == null) {
      return null;
    }
    var duplicatedItemAttribute = duplicateItemAttribute(originalAttr.getItemAttribute());
    return toTmsTestPlanAttribute(newTestPlan, duplicatedItemAttribute);
  }

  private ItemAttribute duplicateItemAttribute(ItemAttribute itemAttribute) {
    return new ItemAttribute(itemAttribute.getKey(), itemAttribute.getValue(), itemAttribute.isSystem());
  }

  public TmsTestPlanAttribute convertToTmsTestPlanAttribute(TmsTestPlan tmsTestPlan,
      TmsTestPlanAttributeRQ attribute) {
    if (attribute == null) {
      return null;
    }
    var itemAttribute = toItemAttribute(attribute);
    return toTmsTestPlanAttribute(tmsTestPlan, itemAttribute);
  }
}
