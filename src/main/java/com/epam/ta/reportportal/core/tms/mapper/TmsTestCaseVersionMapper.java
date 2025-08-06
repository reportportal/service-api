package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestCaseVersionMapper {

  default TmsTestCaseVersion createDefaultTestCaseVersion() {
    var testCaseVersion = new TmsTestCaseVersion();
    testCaseVersion.setDefault(true);
    return testCaseVersion;
  }

  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "testCase", ignore = true)
  @Mapping(target = "default", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  void update(@MappingTarget TmsTestCaseVersion target, TmsTestCaseVersion source);

  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "testCase", ignore = true)
  @Mapping(target = "default", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  void patch(@MappingTarget TmsTestCaseVersion target, TmsTestCaseVersion source);
}
