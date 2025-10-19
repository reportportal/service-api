package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.entity.tms.TmsTestCaseExecution;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between TmsTestCaseExecution entity and TmsTestCaseExecutionRS DTO.
 * Used to convert execution information when populating lastExecution field in TmsTestCaseRS.
 */
@Mapper(config = CommonMapperConfig.class)
public interface TmsTestCaseExecutionMapper {

  /**
   * Converts TmsTestCaseExecution entity to TmsTestCaseExecutionRS DTO.
   * Maps relevant execution information from the entity to the response DTO.
   *
   * @param execution the TmsTestCaseExecution entity to convert
   * @return the converted TmsTestCaseExecutionRS DTO, or null if input is null
   */
  @Mapping(target = "testItemId", source = "testItem.itemId")
  @Mapping(target = "status", source = "testItem.status")  
  @Mapping(target = "name", source = "testItem.name")
  @Mapping(target = "description", source = "testItem.description")
  @Mapping(target = "createdAt", source = "testItem.startTime")
  @Mapping(target = "updatedAt", source = "testItem.endTime")
  @Mapping(target = "executionComment", ignore = true)
  @Mapping(target = "testFolder", ignore = true)
  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "priority", ignore = true)
  TmsTestCaseExecutionRS convert(TmsTestCaseExecution execution);
}