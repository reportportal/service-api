package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.DuplicateTmsTestFolderRS;
import com.epam.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.core.tms.dto.batch.BatchFolderOperationResultRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.core.tms.statistics.FolderDuplicationStatistics;
import com.epam.reportportal.core.tms.statistics.TestCaseDuplicationStatistics;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolderWithCountOfTestCases;
import com.epam.reportportal.ws.converter.PagedResourcesAssembler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

/**
 * Mapper for TmsTestFolder entity and related DTOs.
 * Handles conversion between entity and DTO representations without subfolder hierarchy.
 */
@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestFolderMapper {

  /**
   * Converts TmsTestFolder entity to response DTO without subfolders.
   *
   * @param tmsTestFolder The test folder entity
   * @return The test folder response DTO
   */
  @Mapping(target = "id", source = "tmsTestFolder.id")
  @Mapping(target = "name", source = "tmsTestFolder.name")
  @Mapping(target = "description", source = "tmsTestFolder.description")
  @Mapping(target = "countOfTestCases", ignore = true)
  @Mapping(target = "parentFolderId", source = "tmsTestFolder.parentTestFolder.id")
  public abstract TmsTestFolderRS convertFromTmsTestFolderToRS(TmsTestFolder tmsTestFolder);

  /**
   * Converts TmsTestFolder entity with test case count to response DTO.
   *
   * @param tmsTestFolder    The test folder entity
   * @param countOfTestCases The count of test cases in the folder
   * @return The test folder response DTO
   */
  @Mapping(target = "id", source = "tmsTestFolder.id")
  @Mapping(target = "name", source = "tmsTestFolder.name")
  @Mapping(target = "description", source = "tmsTestFolder.description")
  @Mapping(target = "countOfTestCases", source = "countOfTestCases")
  @Mapping(target = "parentFolderId", source = "tmsTestFolder.parentTestFolder.id")
  public abstract TmsTestFolderRS convertFromTmsTestFolderWithCountOfTestCasesToRS(
      TmsTestFolder tmsTestFolder, Long countOfTestCases);

  /**
   * Converts TmsTestFolderWithCountOfTestCases wrapper to response DTO.
   *
   * @param tmsTestFolderWithCountOfTestCases The wrapper containing folder and test case count
   * @return The test folder response DTO
   */
  public TmsTestFolderRS convertFromTmsTestFolderWithCountOfTestCasesToRS(
      TmsTestFolderWithCountOfTestCases tmsTestFolderWithCountOfTestCases) {
    return convertFromTmsTestFolderWithCountOfTestCasesToRS(
        tmsTestFolderWithCountOfTestCases.getTestFolder(),
        tmsTestFolderWithCountOfTestCases.getCountOfTestCases());
  }

  /**
   * Converts list of TmsTestFolderWithCountOfTestCases to list of response DTOs.
   *
   * @param tmsTestFoldersWithCountOfTestCases List of folders with test case counts
   * @return List of test folder response DTOs
   */
  public List<TmsTestFolderRS> convertFromTmsTestFoldersWithCountOfTestCasesToListOfRS(
      List<TmsTestFolderWithCountOfTestCases> tmsTestFoldersWithCountOfTestCases) {
    return Optional
        .ofNullable(tmsTestFoldersWithCountOfTestCases)
        .orElse(Collections.emptyList())
        .stream()
        .map(this::convertFromTmsTestFolderWithCountOfTestCasesToRS)
        .toList();
  }

  /**
   * Converts paginated test folders to paginated response DTOs.
   *
   * @param testFolders Page of test folders with test case counts
   * @return Paginated test folder response DTOs
   */
  public com.epam.reportportal.model.Page<TmsTestFolderRS> convert(
      Page<TmsTestFolderWithCountOfTestCases> testFolders) {
    return PagedResourcesAssembler
        .pageMultiConverter(this::convertFromTmsTestFoldersWithCountOfTestCasesToListOfRS)
        .apply(testFolders);
  }

  /**
   * Converts request DTO to TmsTestFolder entity.
   *
   * @param projectId The ID of the project
   * @param inputDto  The test folder request DTO
   * @return The test folder entity
   */
  @Mapping(target = "parentTestFolder", ignore = true)
  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "name", source = "inputDto.name")
  @Mapping(target = "description", source = "inputDto.description")
  public abstract TmsTestFolder convertFromRQ(Long projectId, TmsTestFolderRQ inputDto);

  /**
   * Creates a TmsTestFolder entity from folder name.
   *
   * @param projectId The ID of the project
   * @param name      The folder name
   * @return The test folder entity
   */
  @Mapping(target = "name", source = "name")
  @Mapping(target = "project.id", source = "projectId")
  public abstract TmsTestFolder convertFromName(Long projectId, String name);

  /**
   * Creates a TmsTestFolder entity with only ID set.
   *
   * @param id The folder ID
   * @return The test folder entity with only ID
   */
  @Mapping(target = "id", source = "id")
  public abstract TmsTestFolder convertFromId(Long id);

  /**
   * Updates all fields of existing test folder with values from source folder.
   *
   * @param existingTestFolder The target folder to update
   * @param tmsTestFolder      The source folder with new values
   */
  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "parentTestFolder", ignore = true)
  public abstract void update(@MappingTarget TmsTestFolder existingTestFolder,
      TmsTestFolder tmsTestFolder);

  /**
   * Patches existing test folder with non-null values from source folder.
   *
   * @param existingTestFolder The target folder to patch
   * @param tmsTestFolder      The source folder with new values
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "parentTestFolder", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "subFolders", ignore = true)
  public abstract void patch(@MappingTarget TmsTestFolder existingTestFolder,
      TmsTestFolder tmsTestFolder);

  /**
   * Converts NewTestFolderRQ to TmsTestFolderRQ.
   *
   * @param testFolderRQ The new test folder request
   * @return The test folder request DTO
   */
  @Mapping(target = "name", source = "name")
  @Mapping(target = "parentTestFolderId", source = "parentTestFolderId")
  public abstract TmsTestFolderRQ convertToRQ(NewTestFolderRQ testFolderRQ);

  /**
   * Converts ParentTmsTestFolderRQ to TmsTestFolder entity.
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderRQ The parent folder request
   * @return The test folder entity
   */
  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "name", source = "parentTestFolderRQ.name")
  @Mapping(target = "parentTestFolder.id", source = "parentTestFolderRQ.parentTestFolderId")
  public abstract TmsTestFolder convertToTestFolder(Long projectId,
      NewTestFolderRQ parentTestFolderRQ);

  /**
   * Converts folder name to NewTestFolderRQ.
   *
   * @param name The folder name
   * @return The new test folder request
   */
  public abstract NewTestFolderRQ convertToTmsTestCaseTestFolderRQ(String name);

  /**
   * Converts test folder entity with statistics to duplicate response DTO.
   *
   * @param folder                       The duplicated root folder
   * @param countOfTestCases             Count of test cases in the folder
   * @param folderDuplicationStatistics  Statistics of folder duplication
   * @param testCaseDuplicationStatistics Statistics of test case duplication
   * @return The duplicate test folder response DTO
   */
  public DuplicateTmsTestFolderRS convertToDuplicateTmsTestFolderRS(
      TmsTestFolder folder,
      Long countOfTestCases,
      FolderDuplicationStatistics folderDuplicationStatistics,
      TestCaseDuplicationStatistics testCaseDuplicationStatistics) {

    return DuplicateTmsTestFolderRS.builder()
        .id(folder.getId())
        .name(folder.getName())
        .description(folder.getDescription())
        .countOfTestCases(countOfTestCases)
        .parentFolderId(folder.getParentTestFolder() != null
            ? folder.getParentTestFolder().getId()
            : null)
        .folderDuplicationStatistic(convertToFolderOperationResult(folderDuplicationStatistics))
        .testCaseDuplicationStatistic(convertToTestCaseOperationResult(testCaseDuplicationStatistics))
        .build();
  }

  /**
   * Converts folder duplication statistics to batch operation result.
   *
   * @param statistics The folder duplication statistics
   * @return The batch folder operation result
   */
  public BatchFolderOperationResultRS convertToFolderOperationResult(
      FolderDuplicationStatistics statistics) {

    return BatchFolderOperationResultRS.builder()
        .totalCount(statistics.getTotalCount())
        .successCount(statistics.getSuccessCount())
        .failureCount(statistics.getFailureCount())
        .successFolderIds(new ArrayList<>(statistics.getSuccessFolderIds()))
        .errors(new ArrayList<>(statistics.getErrors()))
        .build();
  }

  /**
   * Converts test case duplication statistics to batch operation result.
   *
   * @param statistics The test case duplication statistics
   * @return The batch test case operation result
   */
  public BatchTestCaseOperationResultRS convertToTestCaseOperationResult(
      TestCaseDuplicationStatistics statistics) {

    return BatchTestCaseOperationResultRS.builder()
        .totalCount(statistics.getTotalCount())
        .successCount(statistics.getSuccessCount())
        .failureCount(statistics.getFailureCount())
        .successTestCaseIds(new ArrayList<>(statistics.getSuccessTestCaseIds()))
        .errors(new ArrayList<>(statistics.getErrors()))
        .build();
  }

  /**
   * Duplicates a test folder entity (creates a shallow copy without subfolders and test cases).
   *
   * @param sourceFolder The source folder to duplicate
   * @param targetParent The target parent folder
   * @return The duplicated folder entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "description", source = "sourceFolder.description")
  @Mapping(target = "parentTestFolder", source = "targetParent")
  @Mapping(target = "project", source = "sourceFolder.project")
  @Mapping(target = "subFolders", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "testItems", ignore = true)
  public abstract TmsTestFolder duplicateTestFolder(
      TmsTestFolder sourceFolder,
      TmsTestFolder targetParent
  );
}
