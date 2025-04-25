package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfSubfolders;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestFolderMapper implements DtoMapper<TmsTestFolder, TmsTestFolderRS> {

  @Mapping(target = "id", source = "tmsTestFolder.id")
  @Mapping(target = "name", source = "tmsTestFolder.name")
  @Mapping(target = "description", source = "tmsTestFolder.description")
  @Mapping(target = "countOfSubfolders", source = "countOfSubfolders")
  public abstract TmsTestFolderRS convertToRS(TmsTestFolder tmsTestFolder,
      Long countOfSubfolders);

  public TmsTestFolderRS convertToRS(TmsTestFolder tmsTestFolder) {
    return convertToRS(tmsTestFolder, 0L);
  }

  public TmsTestFolderRS convertToRS(
      TmsTestFolderWithCountOfSubfolders tmsTestFolderWithCountOfSubfolders) {
    return convertToRS(tmsTestFolderWithCountOfSubfolders.getTestFolder(),
        tmsTestFolderWithCountOfSubfolders.getCountOfSubfolders());
  }

  public Page<TmsTestFolderRS> convertToRS(Page<TmsTestFolderWithCountOfSubfolders> testFolders) {
    var content = testFolders.map(this::convertToRS).getContent();
    return new PageImpl<>(content, testFolders.getPageable(),
        testFolders.getTotalElements());
  }

  @Mapping(target = "parentTestFolder", ignore = true)
  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "name", source = "inputDto.name")
  @Mapping(target = "description", source = "inputDto.description")
  public abstract TmsTestFolder convertFromRQ(Long projectId, TmsTestFolderRQ inputDto);

  @Mapping(target = "name", source = "name")
  @Mapping(target = "project.id", source = "projectId")
  public abstract TmsTestFolder convertFromName(Long projectId, String name);

  @Mapping(target = "id", source = "id")
  public abstract TmsTestFolder convertFromId(Long id);

  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "parentTestFolder", ignore = true)
  public abstract void update(@MappingTarget TmsTestFolder existingTestFolder,
      TmsTestFolder tmsTestFolder);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "parentTestFolder", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "subTestFolders", ignore = true)
  public abstract void patch(@MappingTarget TmsTestFolder existingTestFolder,
      TmsTestFolder tmsTestFolder);
}
