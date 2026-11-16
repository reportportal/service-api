package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsRequirementRQ;
import com.epam.reportportal.base.core.tms.dto.TmsRequirementRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioRequirement;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between TmsManualScenarioRequirement entity and its DTOs.
 */
@Mapper(config = CommonMapperConfig.class)
public interface TmsManualScenarioRequirementMapper {

  /**
   * Converts request DTO to entity for creation operations.
   *
   * @param request the requirement request DTO
   * @return the requirement entity
   */
  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "number", ignore = true)
  TmsManualScenarioRequirement toEntity(TmsRequirementRQ request);

  /**
   * Converts entity to response DTO.
   *
   * @param entity the requirement entity
   * @return the requirement response DTO
   */
  TmsRequirementRS toResponse(TmsManualScenarioRequirement entity);

  /**
   * Converts a collection of entities to a list of response DTOs.
   *
   * @param entities the collection of requirement entities
   * @return the list of requirement response DTOs
   */
  List<TmsRequirementRS> toResponseList(Collection<TmsManualScenarioRequirement> entities);

  /**
   * Duplicates a requirement entity for a new manual scenario.
   *
   * @param original the original requirement entity
   * @return the duplicated requirement entity
   */
  @Mapping(target = "manualScenario", ignore = true)
  TmsManualScenarioRequirement duplicate(TmsManualScenarioRequirement original);
}
