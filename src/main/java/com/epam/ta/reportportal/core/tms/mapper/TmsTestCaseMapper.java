package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.entity.project.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestCaseMapper implements DtoMapper<TmsTestCase, TmsTestCaseRS> {

  @Mapping(target = "testFolder", expression = "java(convertToTmsTestFolder(tmsTestCaseRQ.getTestFolderId(), projectId))")
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "versions", ignore = true)
  public abstract TmsTestCase convertFromRQ(Long projectId, TmsTestCaseRQ tmsTestCaseRQ);

  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  @Mapping(target = "id", ignore = true)
  public abstract void update(@MappingTarget TmsTestCase targetTestCase, TmsTestCase tmsTestCase);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  public abstract void patch(@MappingTarget TmsTestCase existingTestCase,
      TmsTestCase tmsTestCase);

  protected TmsTestFolder convertToTmsTestFolder(Long tmsTestFolderId,
      Long projectId) {  //TODO use tmsTestFolderMapper
    if (tmsTestFolderId == null) {
      return null;
    }
    var tmsTestFolder = new TmsTestFolder();

    var project = new Project();
    project.setId(projectId);
    tmsTestFolder.setId(tmsTestFolderId);
    tmsTestFolder.setProject(project); //TODO refactor that

    return tmsTestFolder;
  }
}
