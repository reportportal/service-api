package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.entity.project.Project;
import java.util.Collection;
import java.util.Map;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Mapper(config = CommonMapperConfig.class, uses = TmsTestCaseAttributeMapper.class)
public abstract class TmsTestCaseMapper implements DtoMapper<TmsTestCase, TmsTestCaseRS> {

  @Autowired
  protected TmsManualScenarioMapper tmsManualScenarioMapper;

  @Mapping(target = "manualScenario",
      expression = "java(tmsManualScenarioMapper.convert(defaultCaseVersion.getManualScenario()))")
  @Mapping(target = "id", source = "tmsTestCase.id")
  @Mapping(target = "name", source = "tmsTestCase.name")
  public abstract TmsTestCaseRS convert(TmsTestCase tmsTestCase,
      TmsTestCaseVersion defaultCaseVersion);

  public Page<TmsTestCaseRS> convert(
      Collection<TmsTestCase> testCases,
      Map<Long, TmsTestCaseVersion> testCaseDefaultVersions,
      Pageable pageable) {
    var tmsTestCaseRSList = testCases
        .stream()
        .map(testCase -> convert(testCase, testCaseDefaultVersions.get(testCase.getId())))
        .toList();
    return new PageImpl<>(
        tmsTestCaseRSList, pageable, tmsTestCaseRSList.size()
    );
  }

  @Mapping(target = "testFolder", expression = "java(convertToTmsTestFolder(testFolderId, projectId))")
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "versions", ignore = true)
  public abstract TmsTestCase convertFromRQ(Long projectId, TmsTestCaseRQ tmsTestCaseRQ,
      Long testFolderId);

  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "dataset", ignore = true)
  @Mapping(target = "versions", ignore = true)
  @Mapping(target = "testItems", ignore = true)
  public abstract void update(@MappingTarget TmsTestCase targetTestCase, TmsTestCase tmsTestCase);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "dataset", ignore = true)
  @Mapping(target = "versions", ignore = true)
  @Mapping(target = "testItems", ignore = true)
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
