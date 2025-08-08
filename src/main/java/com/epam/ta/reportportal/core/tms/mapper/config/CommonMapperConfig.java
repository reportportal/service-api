package com.epam.ta.reportportal.core.tms.mapper.config;

import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;

@MapperConfig(
    collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface CommonMapperConfig {

}
