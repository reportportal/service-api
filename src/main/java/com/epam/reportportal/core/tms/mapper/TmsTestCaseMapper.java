package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionInTestPlanRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionLaunchRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Mapper(config = CommonMapperConfig.class, uses = {
    TmsTestCaseAttributeMapper.class
})
public abstract class TmsTestCaseMapper implements DtoMapper<TmsTestCase, TmsTestCaseRS> {

  @Autowired
  protected TmsManualScenarioMapper tmsManualScenarioMapper;

  @Mapping(target = "manualScenario",
      expression = "java(tmsManualScenarioMapper.convert(defaultCaseVersion.getManualScenario()))")
  @Mapping(target = "id", source = "tmsTestCase.id")
  @Mapping(target = "name", source = "tmsTestCase.name")
  public abstract TmsTestCaseRS convert(
      TmsTestCase tmsTestCase,
      TmsTestCaseVersion defaultCaseVersion
  );

  @Mapping(target = "manualScenario",
      expression = "java(tmsManualScenarioMapper.convert(defaultCaseVersion.getManualScenario()))")
  @Mapping(target = "id", source = "tmsTestCase.id")
  @Mapping(target = "name", source = "tmsTestCase.name")
  @Mapping(target = "priority", source = "tmsTestCase.priority")
  @Mapping(target = "lastExecutionAt", source = "lastTestCaseExecution.testItem.startTime")
  public abstract TmsTestCaseRS convert(
      TmsTestCase tmsTestCase,
      TmsTestCaseVersion defaultCaseVersion,
      TmsTestCaseExecution lastTestCaseExecution
  );

  public Page<TmsTestCaseRS> convert(
      Collection<TmsTestCase> testCases,
      Map<Long, TmsTestCaseVersion> testCaseDefaultVersions,
      Map<Long, TmsTestCaseExecution> testCaseExecutions,
      Pageable pageable,
      long totalCount) {
    var tmsTestCaseRSList = testCases
        .stream()
        .map(testCase -> convert(
            testCase,
            testCaseDefaultVersions.get(testCase.getId()),
            testCaseExecutions.get(testCase.getId())))
        .toList();
    return new PageImpl<>(
        tmsTestCaseRSList, pageable, totalCount
    );
  }

  @Mapping(target = "testFolder", expression = "java(convertToTmsTestFolder(testFolderId, projectId))")
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "versions", ignore = true)
  public abstract TmsTestCase convertFromRQ(Long projectId, TmsTestCaseRQ tmsTestCaseRQ,
      Long testFolderId);

  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "dataset", ignore = true)
  @Mapping(target = "versions", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract void update(@MappingTarget TmsTestCase targetTestCase, TmsTestCase tmsTestCase);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "dataset", ignore = true)
  @Mapping(target = "versions", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
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

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "dataset", ignore = true)
  @Mapping(target = "testPlans", ignore = true)
  @Mapping(target = "versions", ignore = true)
  @Mapping(target = "externalId", ignore = true) //externalId is unique
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "name", source = "originalTestCase.name")
  @Mapping(target = "priority", source = "originalTestCase.priority")
  @Mapping(target = "description", source = "originalTestCase.description")
  @Mapping(target = "testFolder", source = "targetFolder")
  public abstract TmsTestCase duplicateTestCase(TmsTestCase originalTestCase,
      TmsTestFolder targetFolder);

  public BatchTestCaseOperationResultRS toBatchOperationResult(List<Long> successfulIds,
      List<BatchTestCaseOperationError> errors) {
    BatchTestCaseOperationResultRS result = new BatchTestCaseOperationResultRS();
    result.setSuccessCount(successfulIds.size());
    result.setFailureCount(errors.size());
    result.setTotalCount(successfulIds.size() + errors.size());
    result.setErrors(errors);
    result.setSuccessTestCaseIds(successfulIds);
    return result;
  }

  /**
   * Converts test case to TmsTestCaseInTestPlanRS with last execution only (for list endpoint).
   *
   * @param testCase      the test case entity
   * @param version       the test case version
   * @param lastExecution the last execution (can be null)
   * @return TmsTestCaseInTestPlanRS with lastExecution populated, executions is null
   */
  @Mapping(target = "id", source = "testCase.id")
  @Mapping(target = "name", source = "testCase.name")
  @Mapping(target = "description", source = "testCase.description")
  @Mapping(target = "priority", source = "testCase.priority")
  @Mapping(target = "externalId", source = "testCase.externalId")
  @Mapping(target = "createdAt", source = "testCase.createdAt", qualifiedByName = "instantToMillis")
  @Mapping(target = "updatedAt", source = "testCase.updatedAt", qualifiedByName = "instantToMillis")
  @Mapping(target = "testFolder", source = "testCase.testFolder")
  @Mapping(target = "manualScenario", expression = "java(tmsManualScenarioMapper.convert(version.getManualScenario()))")
  @Mapping(target = "attributes", source = "testCase.attributes")
  @Mapping(target = "lastExecution", expression = "java(toExecutionInTestPlanRS(lastExecution, launch))")
  @Mapping(target = "executions", ignore = true)
  public abstract TmsTestCaseInTestPlanRS convertToTestCaseInTestPlanRS(
      TmsTestCase testCase,
      TmsTestCaseVersion version,
      TmsTestCaseExecution lastExecution,
      Launch launch
  );

  /**
   * Converts test case to TmsTestCaseInTestPlanRS with full execution history (for single item
   * endpoint).
   *
   * @param testCase      the test case entity
   * @param version       the test case version
   * @param lastExecution the last execution (can be null)
   * @param allExecutions all executions list
   * @return TmsTestCaseInTestPlanRS with both lastExecution and executions populated
   */
  @Mapping(target = "id", source = "testCase.id")
  @Mapping(target = "name", source = "testCase.name")
  @Mapping(target = "description", source = "testCase.description")
  @Mapping(target = "priority", source = "testCase.priority")
  @Mapping(target = "externalId", source = "testCase.externalId")
  @Mapping(target = "createdAt", source = "testCase.createdAt", qualifiedByName = "instantToMillis")
  @Mapping(target = "updatedAt", source = "testCase.updatedAt", qualifiedByName = "instantToMillis")
  @Mapping(target = "testFolder", source = "testCase.testFolder")
  @Mapping(target = "manualScenario", expression = "java(tmsManualScenarioMapper.convert(version.getManualScenario()))")
  @Mapping(target = "attributes", source = "testCase.attributes")
  @Mapping(target = "lastExecution",
      expression = "java(toExecutionInTestPlanRS(lastExecution, lastExecution != null ? launches.get(lastExecution.getLaunchId()) : null))")
  @Mapping(target = "executions", expression = "java(executionsToSet(allExecutions, launches))")
  public abstract TmsTestCaseInTestPlanRS convertToTestCaseInTestPlanRS(
      TmsTestCase testCase,
      TmsTestCaseVersion version,
      TmsTestCaseExecution lastExecution,
      List<TmsTestCaseExecution> allExecutions,
      Map<Long, Launch> launches
  );

  /**
   * Converts TmsTestCaseExecution to TmsTestCaseExecutionInTestPlanRS.
   *
   * @param execution the execution entity
   * @return TmsTestCaseExecutionInTestPlanRS
   */
  @Mapping(target = "id", source = "execution.id")
  @Mapping(target = "launch", expression = "java(testItemToLaunchRS(launch))")
  @Mapping(target = "status", source = "execution.testItem.itemResults.status")
  @Mapping(target = "startedAt", source = "execution.testItem.startTime", qualifiedByName = "instantToMillis")
  @Mapping(target = "finishedAt", source = "execution.testItem.itemResults.endTime", qualifiedByName = "instantToMillis")
  @Mapping(target = "duration", source = "execution.testItem.itemResults.duration")
  public abstract TmsTestCaseExecutionInTestPlanRS toExecutionInTestPlanRS(
      TmsTestCaseExecution execution, Launch launch);

  /**
   * Converts Launch to TmsTestCaseExecutionLaunchRS
   *
   * @param launch the launch
   * @return TmsTestCaseExecutionLaunchRS
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "number", source = "number")
  public abstract TmsTestCaseExecutionLaunchRS testItemToLaunchRS(
      Launch launch);

  /**
   * Converts Instant to milliseconds (epoch millis).
   *
   * @param instant the instant
   * @return epoch milliseconds or null
   */
  @Named("instantToMillis")
  protected Long instantToMillis(java.time.Instant instant) {
    return instant != null ? instant.toEpochMilli() : null;
  }


  protected Set<TmsTestCaseExecutionInTestPlanRS> executionsToSet(
      List<TmsTestCaseExecution> executions,
      Map<Long, Launch> launches) {
    if (executions == null) {
      return null;
    }
    return executions
        .stream()
        .map(execution -> toExecutionInTestPlanRS(
            execution, launches.get(execution.getLaunchId())))
        .collect(Collectors.toSet());
  }
}
