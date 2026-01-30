package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public interface TmsAttributeMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project.id", source = "projectId")
  TmsAttribute convertToTmsAttribute(TmsAttributeRQ request, Long projectId);

  TmsAttributeRS convertToTmsAttributeRS(TmsAttribute entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project", ignore = true)
  void patch(@MappingTarget TmsAttribute entity, TmsAttributeRQ request);

  default TmsAttribute convertToTmsAttribute(Long projectId, String key) {
    var attribute = new TmsAttribute();
    attribute.setKey(key);

    var project = new Project();
    project.setId(projectId);
    attribute.setProject(project);

    return attribute;
  }

  default TmsAttribute convertToTmsAttribute(Long projectId, String key, String value) {
    var attribute = convertToTmsAttribute(projectId, key);
    attribute.setValue(value);
    return attribute;
  }
}
