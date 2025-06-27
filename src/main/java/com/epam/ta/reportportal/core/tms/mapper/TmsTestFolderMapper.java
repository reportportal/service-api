package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfTestCases;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import java.util.List;
import java.util.Map;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestFolderMapper {

  @Mapping(target = "id", source = "tmsTestFolder.id")
  @Mapping(target = "name", source = "tmsTestFolder.name")
  @Mapping(target = "description", source = "tmsTestFolder.description")
  @Mapping(target = "countOfTestCases", ignore = true)
  @Mapping(target = "subFolders", source = "tmsTestFolder.subFolders",
      qualifiedByName = "mapSubFolders")
  @Mapping(target = "parentFolderId", source = "tmsTestFolder.parentTestFolder.id")
  public abstract TmsTestFolderRS convertFromTmsTestFolderToRS(TmsTestFolder tmsTestFolder);

  @Mapping(target = "id", source = "tmsTestFolder.id")
  @Mapping(target = "name", source = "tmsTestFolder.name")
  @Mapping(target = "description", source = "tmsTestFolder.description")
  @Mapping(target = "countOfTestCases", source = "countOfTestCases")
  @Mapping(target = "subFolders", source = "tmsTestFolder.subFolders",
      qualifiedByName = "mapSubFolders")
  @Mapping(target = "parentFolderId", source = "tmsTestFolder.parentTestFolder.id")
  public abstract TmsTestFolderRS convertFromTmsTestFolderWithCountOfTestCasesToRS(
      TmsTestFolder tmsTestFolder, Long countOfTestCases);

  @Named("mapSubFolders")
  public List<TmsTestFolderRS> mapSubFolders(List<TmsTestFolder> subFolders) {
    if (CollectionUtils.isEmpty(subFolders)) {
      return null;
    }
    return subFolders
        .stream()
        .map(this::convertFromSubfolderToRS)
        .toList();
  }

  @Mapping(target = "id", source = "tmsTestFolder.id")
  @Mapping(target = "name", source = "tmsTestFolder.name")
  @Mapping(target = "description", source = "tmsTestFolder.description")
  @Mapping(target = "countOfTestCases", ignore = true)
  @Mapping(target = "subFolders", ignore = true)
  @Mapping(target = "parentFolderId", source = "tmsTestFolder.parentTestFolder.id")
  public abstract TmsTestFolderRS convertFromSubfolderToRS(TmsTestFolder tmsTestFolder);

  public List<TmsTestFolderRS> convertFromTmsTestFoldersWithCountOfTestCasesToListOfRS(
      List<TmsTestFolderWithCountOfTestCases> tmsTestFoldersWithCountOfTestCases) {
    if (CollectionUtils.isEmpty(tmsTestFoldersWithCountOfTestCases)) {
      return null;
    }
    return tmsTestFoldersWithCountOfTestCases
        .stream()
        .map(this::convertFromTmsTestFolderWithCountOfTestCasesToRS)
        .toList();
  }

  public TmsTestFolderRS convertFromTmsTestFolderWithCountOfTestCasesToRS(
      TmsTestFolderWithCountOfTestCases tmsTestFolderWithCountOfTestCases) {
    return convertFromTmsTestFolderWithCountOfTestCasesToRS(
        tmsTestFolderWithCountOfTestCases.getTestFolder(),
        tmsTestFolderWithCountOfTestCases.getCountOfTestCases());
  }

  public com.epam.ta.reportportal.model.Page<TmsTestFolderRS> convert(
      Page<TmsTestFolderWithCountOfTestCases> testFolders) {
    return PagedResourcesAssembler
        .pageMultiConverter(this::convertFromTmsTestFoldersWithCountOfTestCasesToListOfRS)
        .apply(testFolders);
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
  @Mapping(target = "subFolders", ignore = true)
  public abstract void patch(@MappingTarget TmsTestFolder existingTestFolder,
      TmsTestFolder tmsTestFolder);

  public com.epam.ta.reportportal.model.Page<TmsTestFolderRS> convert(
      Page<TmsTestFolderWithCountOfTestCases> pageOfTestFolders,
      Map<Long, Long> testCaseCountForSubfolders) {
    return PagedResourcesAssembler
        .pageMultiConverter(
            (List<TmsTestFolderWithCountOfTestCases> testFolders) -> this.convert(
                testFolders, testCaseCountForSubfolders)
        )
        .apply(pageOfTestFolders);
  }

  public abstract TmsTestFolderRQ convertFromNameToRQ(String name);

  private List<TmsTestFolderRS> convert(
      List<TmsTestFolderWithCountOfTestCases> testFoldersWithCountOfTestCases,
      Map<Long, Long> testCaseCountForSubfolders) {
    if (CollectionUtils.isEmpty(testFoldersWithCountOfTestCases)) {
      return null;
    }
    return testFoldersWithCountOfTestCases
        .stream()
        .map((TmsTestFolderWithCountOfTestCases t) ->
            convertFromTmsTestFolderWithCountOfTestCasesToRS(t, testCaseCountForSubfolders))
        .toList();
  }

  public TmsTestFolderRS convertFromTmsTestFolderWithCountOfTestCasesToRS(
      TmsTestFolderWithCountOfTestCases folderWithCount,
      Map<Long, Long> subFolderTestCaseCounts) {

    if (folderWithCount == null) {
      return null;
    }

    var testFolder = folderWithCount.getTestFolder();
    var result = TmsTestFolderRS.builder()
        .id(testFolder.getId())
        .name(testFolder.getName())
        .description(testFolder.getDescription())
        .countOfTestCases(folderWithCount.getCountOfTestCases())
        .parentFolderId(testFolder.getParentTestFolder() != null ?
            testFolder.getParentTestFolder().getId() : null)
        .build();

    if (!CollectionUtils.isEmpty(testFolder.getSubFolders())) {
      var subFoldersRS = testFolder.getSubFolders()
          .stream()
          .map(subFolder -> mapSubFolderWithTestCaseCount(subFolder, subFolderTestCaseCounts))
          .toList();
      result.setSubFolders(subFoldersRS);
    }

    return result;
  }

  private TmsTestFolderRS mapSubFolderWithTestCaseCount(
      TmsTestFolder subFolder,
      Map<Long, Long> subFolderTestCaseCounts) {

    var testCaseCount = subFolderTestCaseCounts.getOrDefault(subFolder.getId(), 0L);

    return TmsTestFolderRS.builder()
        .id(subFolder.getId())
        .name(subFolder.getName())
        .description(subFolder.getDescription())
        .countOfTestCases(testCaseCount)
        .parentFolderId(subFolder.getParentTestFolder() != null ?
            subFolder.getParentTestFolder().getId() : null)
        .build();
  }
}
