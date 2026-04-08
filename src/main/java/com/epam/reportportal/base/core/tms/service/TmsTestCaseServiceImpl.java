package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_PLAN_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static java.util.Objects.nonNull;

import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.PreparedTestCase;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeImportRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportParseResult;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCasePreparationForImportResult;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDuplicateTestCasesRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCasesRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseActivityResourceMapper;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseMapper;
import com.epam.reportportal.base.core.tms.mapper.factory.TmsTestCaseExporterFactory;
import com.epam.reportportal.base.core.tms.mapper.factory.TmsTestCaseImporterFactory;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsTestCaseFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.PageableUtils;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Valid
@Slf4j
public class TmsTestCaseServiceImpl implements TmsTestCaseService {

  private static final String TEST_CASE_NOT_FOUND_BY_ID = "Test Case with id: %d for projectId: %d";
  private static final String TEST_CASE_NOT_FOUND_IN_TEST_PLAN = "Test Case with id: %d for projectId: %d in test plan: %d";
  private static final String TEST_CASES_NOT_FOUND_BY_IDS = "Test Cases with ids: %s for projectId: %d";
  private static final String TEST_FOLDER_NOT_FOUND_BY_ID =
      "Test Folder with id: %d for project: %d";

  private final TmsTestCaseMapper tmsTestCaseMapper;
  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final TmsTestCaseFilterableRepository tmsTestCaseFilterableRepository;
  private final TmsTestCaseAttributeService tmsTestCaseAttributeService;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TmsTestCaseImporterFactory importerFactory;
  private final TmsTestCaseExporterFactory exporterFactory;
  private final TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final TmsTestCaseActivityResourceMapper tmsTestCaseActivityResourceMapper;

  private TmsTestFolderService tmsTestFolderService;
  private TmsTestCaseExecutionService tmsTestCaseExecutionService;
  private TmsManualLaunchService tmsManualLaunchService;
  private TmsAttributeService tmsAttributeService;

  @Autowired
  public void setTmsTestFolderService(
      TmsTestFolderService tmsTestFolderService) {
    this.tmsTestFolderService = tmsTestFolderService;
  }

  @Autowired
  public void setTmsTestCaseExecutionService(
      TmsTestCaseExecutionService tmsTestCaseExecutionService) {
    this.tmsTestCaseExecutionService = tmsTestCaseExecutionService;
  }

  @Autowired
  public void setTmsManualLaunchService(
      TmsManualLaunchService tmsManualLaunchService) {
    this.tmsManualLaunchService = tmsManualLaunchService;
  }

  @Autowired
  public void setTmsAttributeService(TmsAttributeService tmsAttributeService) {
    this.tmsAttributeService = tmsAttributeService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestCaseRS> getTestCaseByProjectId(long projectId) {
    return tmsTestCaseRepository
        .findByProjectId(projectId)
        .stream()
        .map(tmsTestCaseMapper::convert)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseRS getById(long projectId, Long testCaseId) {
    return tmsTestCaseMapper.convert(
        tmsTestCaseRepository
            .findByProjectIdAndId(projectId, testCaseId)
            .orElseThrow(() -> new ReportPortalException(
                NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
            ),
        tmsTestCaseVersionService.getDefaultVersion(testCaseId),
        tmsTestCaseExecutionService.getLastTestCaseExecution(testCaseId));
  }

  @Override
  @Transactional
  public TmsTestCaseRS create(MembershipDetails membershipDetails,
      ReportPortalUser user,
      TmsTestCaseRQ tmsTestCaseRQ) {
    var projectId = membershipDetails.getProjectId();
    var tmsTestCase = tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
        getTestFolderId(
            projectId,
            tmsTestCaseRQ.getTestFolderId(),
            tmsTestCaseRQ.getTestFolder()
        )
    );

    tmsTestCaseRepository.save(tmsTestCase);

    if (CollectionUtils.isNotEmpty(tmsTestCaseRQ.getAttributes())) {
      tmsTestCaseAttributeService.createTestCaseAttributes(projectId, tmsTestCase,
          tmsTestCaseRQ.getAttributes());
    }

    var defaultVersion = tmsTestCaseVersionService.createDefaultTestCaseVersion(projectId,
        tmsTestCase,
        tmsTestCaseRQ.getManualScenario());

    var after = tmsTestCaseActivityResourceMapper.buildActivityResource(tmsTestCase,
        defaultVersion);

    eventPublisher.publishEvent(
        tmsTestCaseActivityResourceMapper.buildTestCaseCreatedEvent(membershipDetails, user, after)
    );

    return tmsTestCaseMapper.convert(tmsTestCase, defaultVersion);
  }

  @Override
  @Transactional
  public TmsTestCaseRS update(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ) {
    var projectId = membershipDetails.getProjectId();

    return tmsTestCaseRepository
        .findByProjectIdAndId(projectId, testCaseId)
        .map((var existingTestCase) -> {
          var beforeVersion = tmsTestCaseVersionService.getDefaultVersion(existingTestCase.getId());
          var before = tmsTestCaseActivityResourceMapper.buildActivityResource(existingTestCase,
              beforeVersion);

          tmsTestCaseMapper.update(existingTestCase,
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolderId(),
                      tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.updateTestCaseAttributes(projectId, existingTestCase,
              tmsTestCaseRQ.getAttributes());

          var defaultVersion = tmsTestCaseVersionService.updateDefaultTestCaseVersion(
              projectId,
              existingTestCase,
              tmsTestCaseRQ.getManualScenario());

          var lastTestCaseExecution = tmsTestCaseExecutionService.getLastTestCaseExecution(
              existingTestCase.getId()
          );

          var after = tmsTestCaseActivityResourceMapper.buildActivityResource(existingTestCase,
              defaultVersion);

          eventPublisher.publishEvent(
              tmsTestCaseActivityResourceMapper.buildTestCaseUpdatedEvent(
                  membershipDetails,
                  user,
                  before,
                  after
              )
          );

          return tmsTestCaseMapper.convert(
              existingTestCase, defaultVersion, lastTestCaseExecution
          );
        })
        .orElseGet(() -> create(membershipDetails, user, tmsTestCaseRQ));
  }

  @Override
  @Transactional
  public TmsTestCaseRS patch(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ) {
    var projectId = membershipDetails.getProjectId();
    return tmsTestCaseRepository
        .findByProjectIdAndId(projectId, testCaseId)
        .map((var existingTestCase) -> {
          var beforeVersion = tmsTestCaseVersionService.getDefaultVersion(existingTestCase.getId());
          var before = tmsTestCaseActivityResourceMapper.buildActivityResource(existingTestCase,
              beforeVersion);

          tmsTestCaseMapper.patch(existingTestCase,
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolderId(),
                      tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.updateTestCaseAttributes(projectId, existingTestCase,
              tmsTestCaseRQ.getAttributes());

          var defaultVersion = tmsTestCaseVersionService.patchDefaultTestCaseVersion(
              projectId,
              existingTestCase,
              tmsTestCaseRQ.getManualScenario());

          var lastTestCaseExecution = tmsTestCaseExecutionService.getLastTestCaseExecution(
              existingTestCase.getId()
          );

          var after = tmsTestCaseActivityResourceMapper.buildActivityResource(existingTestCase,
              defaultVersion);

          eventPublisher.publishEvent(
              tmsTestCaseActivityResourceMapper.buildTestCaseUpdatedEvent(
                  membershipDetails,
                  user,
                  before,
                  after
              )
          );

          return tmsTestCaseMapper.convert(
              existingTestCase, defaultVersion, lastTestCaseExecution
          );
        })
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
        );
  }

  @Override
  @Transactional
  public void delete(MembershipDetails membershipDetails, ReportPortalUser user, Long testCaseId) {
    tmsTestCaseAttributeService.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseVersionService.deleteAllByTestCaseId(testCaseId);
    tmsTestPlanTestCaseRepository.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseRepository.deleteById(testCaseId);

    eventPublisher.publishEvent(
        tmsTestCaseActivityResourceMapper.buildTestCaseDeletedEvent(membershipDetails, user, testCaseId)
    );
  }

  @Override
  @Transactional
  public void deleteByTestFolderId(MembershipDetails membershipDetails,
      ReportPortalUser user, long folderId) {
    var projectId = membershipDetails.getProjectId();
    var testCaseIds = Optional
        .ofNullable(tmsTestCaseRepository.findIdsByProjectIdAndTestFolderId(projectId, folderId))
        .orElseGet(ArrayList::new);
    tmsTestCaseAttributeService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseVersionService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestPlanTestCaseRepository.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseRepository.deleteTestCasesByFolderId(projectId, folderId);
    testCaseIds.forEach(testCaseId -> eventPublisher.publishEvent(
        tmsTestCaseActivityResourceMapper.buildTestCaseDeletedEvent(membershipDetails, user, testCaseId)
    ));
  }

  @Override
  @Transactional
  public void delete(MembershipDetails membershipDetails, ReportPortalUser user,
      @Valid BatchDeleteTestCasesRQ deleteRequest) {
    var testCaseIds = Optional
        .ofNullable(tmsTestCaseRepository.findIdsByProjectId(membershipDetails.getProjectId()))
        .orElseGet(ArrayList::new);
    tmsTestCaseAttributeService.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestCaseVersionService.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestPlanTestCaseRepository.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestCaseRepository.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    testCaseIds.forEach(testCaseId -> eventPublisher.publishEvent(
        tmsTestCaseActivityResourceMapper.buildTestCaseDeletedEvent(membershipDetails, user, testCaseId)
    ));
  }

  @Override
  @Transactional
  public BatchPatchTestCasesRS patch(long projectId,
      @Valid BatchPatchTestCasesRQ patchRequest) {
    var testCaseIds = patchRequest.getTestCaseIds();
    Long testFolderId = null;
    if (nonNull(patchRequest.getTestFolderId())
        || (nonNull(patchRequest.getTestFolder())
        && nonNull(patchRequest.getTestFolder().getName()))) {
      testFolderId = tmsTestFolderService.resolveTargetFolderId(
          projectId, patchRequest.getTestFolderId(), patchRequest.getTestFolder()
      );
    }
    if (nonNull(testFolderId)
        || nonNull(patchRequest.getPriority())) {
      tmsTestCaseRepository.patch(projectId,
          testCaseIds,
          testFolderId,
          patchRequest.getPriority());
    }
    return tmsTestCaseMapper.toBatchPatchTestCasesRS(
        testFolderId, patchRequest
    );
  }

  @Override
  @Transactional
  public List<TmsTestFolderRS> importFromFile(
      long projectId,
      Long testFolderId,
      String testFolderName,
      MultipartFile file) {

    // 1. Parse file
    var parseResult = parseImportFile(file);
    validateParseResult(parseResult);

    // 2. Resolve base folder
    var baseFolderId = resolveBaseFolderIdForImport(projectId, testFolderId, testFolderName);

    var affectedFolderIds = new HashSet<Long>();
    affectedFolderIds.add(baseFolderId);

    // 3. Single pass: collect paths, keys, and validate
    var preparationResult = prepareTestCasesForImport(parseResult.getTestCases(), baseFolderId);

    // 4. Batch resolve folders and attributes
    var pathToFolderId = tmsTestFolderService.resolveFolderPathsBatch(
        projectId, baseFolderId, preparationResult.getUniquePaths());
    affectedFolderIds.addAll(pathToFolderId.values());
    
    var keyToAttributeId = tmsAttributeService.resolveAttributes(
        projectId, preparationResult.getUniqueAttributeKeys());

    // 5. Assign resolved folder IDs
    assignFolderIds(preparationResult.getPreparedTestCases(), baseFolderId, pathToFolderId);

    // 6. Validate all have folders
    validateFolderAssignment(preparationResult.getPreparedTestCases());

    // 7. Batch create all test cases
    importTestCases(projectId, preparationResult.getPreparedTestCases(),
        keyToAttributeId);
    
    return tmsTestFolderService.getFoldersWithCountByIds(projectId, affectedFolderIds);
  }

  private TmsTestCasePreparationForImportResult prepareTestCasesForImport(
      List<TmsTestCaseImportRQ> testCases,
      Long baseFolderId) {

    var result = new TmsTestCasePreparationForImportResult();

    for (int i = 0; i < testCases.size(); i++) {
      var testCase = testCases.get(i);
      int rowNumber = i + 2;

      // Validate required fields
      if (StringUtils.isBlank(testCase.getName())) {
        result.addError("Row " + rowNumber + ": missing required field 'summary'");
        continue;
      }

      // Compute path key once
      var folderPath = testCase.getFolderPath();
      String pathKey = null;
      if (folderPath != null && !folderPath.isEmpty()) {
        pathKey = String.join("/", folderPath);
        result.addUniquePath(folderPath);
      } else if (baseFolderId == null) {
        result.addError("Row " + rowNumber + " ('" + testCase.getName()
            + "'): no target folder specified");
        continue;
      }

      // Collect attribute keys
      result.addAttributeKeys(testCase.getAttributes());

      // Add prepared test case
      result.addPreparedTestCase(PreparedTestCase.builder()
          .testCase(testCase)
          .rowNumber(rowNumber)
          .pathKey(pathKey)
          .build());
    }

    if (result.hasErrors()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Validation failed:\n  - " + String.join("\n  - ", result.getValidationErrors()));
    }

    return result;
  }

  private void assignFolderIds(
      List<PreparedTestCase> preparedTestCases,
      Long baseFolderId,
      Map<String, Long> pathToFolderId) {

    for (var prepared : preparedTestCases) {
      if (prepared.getPathKey() != null) {
        prepared.setFolderId(pathToFolderId.get(prepared.getPathKey()));
      } else {
        prepared.setFolderId(baseFolderId);
      }
    }
  }

  private void validateFolderAssignment(List<PreparedTestCase> preparedTestCases) {
    var errors = preparedTestCases.stream()
        .filter(p -> p.getFolderId() == null)
        .map(p -> "Row " + p.getRowNumber() + " ('" + p.getTestCase().getName()
            + "'): failed to resolve folder")
        .toList();

    if (!errors.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Folder resolution failed:\n  - " + String.join("\n  - ", errors));
    }
  }

  private List<Long> importTestCases(
      long projectId,
      List<PreparedTestCase> preparedTestCases,
      Map<String, Long> keyToAttributeId) {

    // 1. Create all test case entities
    var testCaseEntities = preparedTestCases.stream()
        .map(prepared -> tmsTestCaseMapper.convertFromImportRQ(
            projectId,
            prepared.getTestCase(),
            prepared.getFolderId()))
        .toList();

    // 2. Batch save all test cases
    var savedTestCases = tmsTestCaseRepository.saveAll(testCaseEntities);

    for (int i = 0; i < savedTestCases.size(); i++) {
      var savedTestCase = savedTestCases.get(i);
      var importRQ = preparedTestCases.get(i).getTestCase();

      if (importRQ.getAttributes() != null) {
        createAttributesFromImport(projectId, savedTestCase, importRQ.getAttributes(),
            keyToAttributeId);
      }

      tmsTestCaseVersionService.createDefaultTestCaseVersion(projectId, savedTestCase,
          importRQ.getManualScenario());
    }

    return savedTestCases
        .stream()
        .map(TmsTestCase::getId)
        .toList();
  }

  private TmsTestCaseImportParseResult parseImportFile(MultipartFile file) {
    var importer = importerFactory.getImporter(file.getOriginalFilename());

    try (var inputStream = file.getInputStream()) {
      return importer.parse(inputStream);
    } catch (IOException e) {
      log.error("Failed to read file: {}", file.getOriginalFilename(), e);
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Failed to read file: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }

  private void validateParseResult(TmsTestCaseImportParseResult parseResult) {
    if (parseResult.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "File contains no valid data rows to import");
    }
  }

  private void createAttributesFromImport(
      long projectId, TmsTestCase testCase,
      List<TmsTestCaseAttributeImportRQ> importAttributes,
      Map<String, Long> keyToAttributeId) {

    List<TmsTestCaseAttributeRQ> attributeRequests = importAttributes.stream()
        .map(TmsTestCaseAttributeImportRQ::getKey)
        .filter(keyToAttributeId::containsKey)
        .map(key -> {
          var attrRQ = new TmsTestCaseAttributeRQ();
          attrRQ.setId(keyToAttributeId.get(key));
          return attrRQ;
        })
        .toList();

    if (!attributeRequests.isEmpty()) {
      tmsTestCaseAttributeService.createTestCaseAttributes(projectId, testCase, attributeRequests);
    }
  }

  /**
   * Resolves base folder ID from API parameters for import.
   */
  private Long resolveBaseFolderIdForImport(long projectId, Long testFolderId,
      String testFolderName) {
    if (testFolderId != null) {
      // Validate folder exists
      if (!tmsTestFolderService.existsById(projectId, testFolderId)) {
        throw new ReportPortalException(NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId));
      }
      return testFolderId;
    }

    if (StringUtils.isNotBlank(testFolderName)) {
      // Create new folder at root level
      return tmsTestFolderService.resolveFolderPath(projectId, null, List.of(testFolderName));
    }

    // No folder specified - will use path from CSV or return null
    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public void exportToFile(Long projectId, List<Long> testCaseIds, String format,
      boolean includeAttachments, HttpServletResponse response) {
    List<TmsTestCaseRS> testCases;

    if (testCaseIds != null && !testCaseIds.isEmpty()) {
      testCases = testCaseIds.stream()
          .map(id -> getById(projectId, id))
          .toList();
    } else {
      testCases = getTestCaseByProjectId(projectId);
    }
    exporterFactory
        .getExporter(format)
        .export(testCases, includeAttachments, response);

  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestCaseRS> getTestCasesByCriteria(long projectId, Filter filter,
      Pageable pageable) {
    var testCaseIds = tmsTestCaseFilterableRepository.findIdsByProjectIdAndFilter(
        projectId, filter, pageable
    );
    if (testCaseIds.hasContent()) {
      var testCaseDefaultVersions = tmsTestCaseVersionService.getDefaultVersions(
          testCaseIds.getContent());
      var testCases = tmsTestCaseRepository
          .findByProjectIdAndIds(projectId, testCaseIds.getContent())
          .stream()
          .collect(Collectors.toMap(TmsTestCase::getId, Function.identity()));

      var orderedTestTestCases = testCaseIds
          .getContent()
          .stream()
          .map(testCases::get)
          .filter(Objects::nonNull)
          .toList();

      var lastTestCasesExecutions = tmsTestCaseExecutionService.getLastTestCasesExecutionsByTestCaseIds(
          testCaseIds.getContent()
      );

      var page = tmsTestCaseMapper.convert(
          orderedTestTestCases,
          testCaseDefaultVersions,
          lastTestCasesExecutions,
          pageable,
          testCaseIds.getTotalElements()
      );

      return PagedResourcesAssembler
          .<TmsTestCaseRS>pageConverter()
          .apply(page);
    } else {
      return PagedResourcesAssembler
          .<TmsTestCaseRS>pageConverter()
          .apply(new PageImpl<>(Collections.emptyList(), pageable, 0));
    }
  }

  @Override
  @Transactional
  public void deleteAttributesFromTestCase(Long projectId, Long testCaseId,
      List<Long> attributeIds) {
    if (!tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)) {
      throw new ReportPortalException(
          ErrorType.NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId)
      );
    }
    tmsTestCaseAttributeService.deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Override
  @Transactional
  public void patchTestCaseAttributes(Long projectId, BatchPatchTestCaseAttributesRQ patchRequest) {
    validateTestCasesExist(projectId, patchRequest.getTestCaseIds());

    var keysToRemove = Optional
        .ofNullable(patchRequest.getAttributeKeysToRemove())
        .orElse(Collections.emptySet());
    var keysToAdd = Optional
        .ofNullable(patchRequest.getAttributeKeysToAdd())
        .orElse(Collections.emptySet());

    var keysSetToRemove = new HashSet<>(keysToRemove);
    var keysSetToAdd = new HashSet<>(keysToAdd);

    var intersection = new HashSet<>(keysSetToRemove);
    intersection.retainAll(keysSetToAdd);

    keysSetToRemove.removeAll(intersection);
    keysSetToAdd.removeAll(intersection);

    if (!keysSetToRemove.isEmpty()) {
      var attributeIdsToRemove = tmsAttributeService.findExistingTagIdsByKeys(
          projectId, keysSetToRemove
      );
      if (!attributeIdsToRemove.isEmpty()) {
        tmsTestCaseAttributeService.deleteByTestCaseIdsAndAttributeIds(
            patchRequest.getTestCaseIds(), attributeIdsToRemove
        );
      }
    }

    if (!keysSetToAdd.isEmpty()) {
      var attributeIdsToAdd = tmsAttributeService.resolveTagIdsByKeys(
          projectId, keysSetToAdd
      );
      if (!attributeIdsToAdd.isEmpty()) {
        tmsTestCaseAttributeService.addAttributesToTestCases(
            patchRequest.getTestCaseIds(), attributeIdsToAdd
        );
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void validateTestCasesExist(Long projectId, List<Long> testCaseIds) {
    var existingTestCaseIds = new HashSet<>(
        tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds)
    );

    var notFoundTestCaseIds = testCaseIds.stream()
        .filter(id -> !existingTestCaseIds.contains(id))
        .toList();

    if (!notFoundTestCaseIds.isEmpty()) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_CASES_NOT_FOUND_BY_IDS.formatted(notFoundTestCaseIds, projectId)
      );
    }
  }

  @Override
  @Transactional
  public BatchDuplicateTestCasesRS duplicate(long projectId,
      BatchDuplicateTestCasesRQ duplicateRequest) {
    validateTestCasesExist(projectId, duplicateRequest.getTestCaseIds());

    var targetFolderId = tmsTestFolderService.resolveTargetFolderId(
        projectId,
        duplicateRequest.getTestFolderId(),
        duplicateRequest.getTestFolder()
    );

    var duplicatedTestCases = duplicateRequest
        .getTestCaseIds()
        .stream()
        .map(testCaseId -> duplicateTestCase(projectId, testCaseId, targetFolderId))
        .toList();
    return BatchDuplicateTestCasesRS.builder()
        .testFolderId(targetFolderId)
        .testCases(duplicatedTestCases)
        .build();
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS duplicateTestCases(long projectId, List<Long> testCaseIds) {
    var errors = new ArrayList<BatchTestCaseOperationError>();
    var successfulIds = new ArrayList<Long>();

    for (var testCaseId : testCaseIds) {
      try {
        var originalTestCase = tmsTestCaseRepository
            .findByProjectIdAndId(projectId, testCaseId)
            .orElseThrow(() -> new ReportPortalException(
                NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
            );

        var originalDefaultVersion = tmsTestCaseVersionService.getDefaultVersion(testCaseId);

        var duplicatedTestCase = tmsTestCaseMapper.duplicateTestCase(
            originalTestCase, originalTestCase.getTestFolder()
        );

        duplicatedTestCase = tmsTestCaseRepository.save(duplicatedTestCase);

        tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase,
            originalDefaultVersion);

        if (CollectionUtils.isNotEmpty(originalTestCase.getAttributes())) {
          tmsTestCaseAttributeService.duplicateTestCaseAttributes(originalTestCase,
              duplicatedTestCase);
        }

        successfulIds.add(duplicatedTestCase.getId());

      } catch (Exception e) {
        errors.add(new BatchTestCaseOperationError(testCaseId,
            "Failed to duplicate test case: " + e.getMessage()));
      }
    }

    return tmsTestCaseMapper.toBatchOperationResult(successfulIds, errors);
  }

  @Override
  public BatchTestCaseOperationResultRS duplicateTestCases(long projectId,
      TmsTestFolder targetFolder, List<Long> testCaseIds) {
    var errors = new ArrayList<BatchTestCaseOperationError>();
    var successfulIds = new ArrayList<Long>();

    for (var testCaseId : testCaseIds) {
      try {
        var originalTestCase = tmsTestCaseRepository
            .findByProjectIdAndId(projectId, testCaseId)
            .orElseThrow(() -> new ReportPortalException(
                NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
            );

        var originalDefaultVersion = tmsTestCaseVersionService.getDefaultVersion(testCaseId);

        var duplicatedTestCase = tmsTestCaseMapper.duplicateTestCase(
            originalTestCase, targetFolder
        );

        duplicatedTestCase = tmsTestCaseRepository.save(duplicatedTestCase);

        tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase,
            originalDefaultVersion);

        if (CollectionUtils.isNotEmpty(originalTestCase.getAttributes())) {
          tmsTestCaseAttributeService.duplicateTestCaseAttributes(originalTestCase,
              duplicatedTestCase);
        }

        successfulIds.add(duplicatedTestCase.getId());

      } catch (Exception e) {
        errors.add(new BatchTestCaseOperationError(testCaseId,
            "Failed to duplicate test case: " + e.getMessage()));
      }
    }

    return tmsTestCaseMapper.toBatchOperationResult(successfulIds, errors);
  }

  private Long getTestFolderId(long projectId, Long testFolderId,
      NewTestFolderRQ testFolderRQ) {
    if (Objects.isNull(testFolderId) && Objects.isNull(testFolderRQ)) {
      return null;
    }
    if (nonNull(testFolderId)) {
      if (tmsTestFolderService.existsById(projectId, testFolderId)) {
        return testFolderId;
      } else {
        throw new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId));
      }
    } else {
      return tmsTestFolderService
          .create(projectId, testFolderRQ)
          .getId();
    }
  }

  @Transactional
  public TmsTestCaseRS duplicateTestCase(long projectId, Long testCaseId, Long targetFolderId) {
    var originalTestCase = tmsTestCaseRepository
        .findByProjectIdAndId(projectId, testCaseId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
        );

    var originalDefaultVersion = tmsTestCaseVersionService.getDefaultVersion(testCaseId);

    var targetFolder = tmsTestFolderService.getEntityById(projectId, targetFolderId);

    var duplicatedTestCase = tmsTestCaseMapper.duplicateTestCase(originalTestCase, targetFolder);

    duplicatedTestCase = tmsTestCaseRepository.save(duplicatedTestCase);

    var duplicatedDefaultVersion = tmsTestCaseVersionService.duplicateDefaultVersion(
        duplicatedTestCase, originalDefaultVersion);

    if (CollectionUtils.isNotEmpty(originalTestCase.getAttributes())) {
      tmsTestCaseAttributeService.duplicateTestCaseAttributes(originalTestCase, duplicatedTestCase);
    }

    return tmsTestCaseMapper.convert(duplicatedTestCase, duplicatedDefaultVersion);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsById(Long projectId, Long testCaseId) {
    return tmsTestCaseRepository.existsByIdAndProjectId(testCaseId, projectId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> getExistingTestCaseIds(Long projectId, List<Long> testCaseIds) {
    if (testCaseIds == null || testCaseIds.isEmpty()) {
      return List.of();
    }
    return tmsTestCaseRepository.findExistingTestCaseIds(projectId, testCaseIds);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestCaseInTestPlanRS> getTestCasesInTestPlan(Long projectId, Long testPlanId,
      Filter filter, Pageable pageable) {

    var enhancedFilter = enhanceFilterWithProjectIdAndTestPlanId(filter, projectId, testPlanId);

    var testCaseIdsPage = tmsTestCaseFilterableRepository.findIdsByFilter(enhancedFilter, pageable);

    if (testCaseIdsPage.isEmpty()) {
      return PagedResourcesAssembler
          .<TmsTestCaseInTestPlanRS>pageConverter()
          .apply(new PageImpl<>(Collections.emptyList(), pageable, 0));
    }

    var testCaseIds = testCaseIdsPage.getContent();

    // Fetch test cases
    var testCases = tmsTestCaseRepository
        .findByProjectIdAndIds(projectId, testCaseIds)
        .stream()
        .collect(Collectors.toMap(TmsTestCase::getId, Function.identity()));

    // Fetch default versions for test cases
    var defaultVersions = tmsTestCaseVersionService.getDefaultVersions(testCaseIds);

    // Fetch ONLY last executions for each test case within this test plan
    var lastExecutionsInTestPlan = tmsTestCaseExecutionService
        .findLastExecutionsByTestCaseIdsAndTestPlanId(testCaseIds, testPlanId);

    var launches = tmsManualLaunchService.getEntitiesByIds(
        projectId,
        lastExecutionsInTestPlan
            .values()
            .stream()
            .map(TmsTestCaseExecution::getLaunchId)
            .toList()
    );

    // Map to response DTOs maintaining order from pagination
    var orderedTestCaseResponses = testCaseIds
        .stream()
        .map(testCases::get)
        .filter(Objects::nonNull)
        .map(tc -> {
          var lastExecutionInTestPlan = lastExecutionsInTestPlan.get(tc.getId());

          return tmsTestCaseMapper.convertToTestCaseInTestPlanRS(
              tc,
              defaultVersions.get(tc.getId()),
              lastExecutionInTestPlan,
              nonNull(lastExecutionInTestPlan) ?
                  launches.get(lastExecutionInTestPlan.getLaunchId())
                  : null
          );
        })
        .toList();

    return PagedResourcesAssembler
        .<TmsTestCaseInTestPlanRS>pageConverter()
        .apply(new PageImpl<>(orderedTestCaseResponses, pageable,
            testCaseIdsPage.getTotalElements()));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseInTestPlanRS getTestCaseInTestPlan(Long projectId, Long testPlanId,
      Long testCaseId) {

    // Fetch a test case and verify it exists in a project
    var testCase = tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
        );

    // Verify test case is added to the test plan
    verifyTestCaseInTestPlan(testPlanId, testCaseId, projectId);

    // Fetch default version
    var defaultVersion = tmsTestCaseVersionService.getDefaultVersion(testCaseId);

    // Fetch ALL executions for this test case within this test plan
    // They are ordered by test_item.start_time DESC, so first is the latest
    var allExecutionsInTestPlan = tmsTestCaseExecutionService
        .findByTestCaseIdAndTestPlanId(testCaseId, testPlanId);

    // Find the last execution (first in the list as they are ordered by start_time DESC)
    var lastExecution = CollectionUtils.isEmpty(allExecutionsInTestPlan) ?
        null : allExecutionsInTestPlan.getFirst();

    var launches = tmsManualLaunchService.getEntitiesByIds(
        projectId,
        allExecutionsInTestPlan
            .stream()
            .map(TmsTestCaseExecution::getLaunchId)
            .toList()
    );

    // Convert to response DTO with both last execution and all executions
    return tmsTestCaseMapper.convertToTestCaseInTestPlanRS(
        testCase,
        defaultVersion,
        lastExecution,
        allExecutionsInTestPlan,
        launches
    );
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCase getEntityById(Long testCaseId) {
    log.debug("Getting test case entity by ID: {}", testCaseId);

    return tmsTestCaseRepository.findById(testCaseId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, "Test case with id: " + testCaseId + " not found")
        );
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestCaseRS> getByIds(long projectId, List<Long> testCaseIds) {
    return tmsTestCaseRepository
        .findByProjectIdAndIds(projectId, testCaseIds)
        .stream()
        .map(tmsTestCaseMapper::convert)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> getTestCaseIdsInTestPlan(long projectId, Long testPlanId) {
    return PageableUtils
        .loadAll(
            pageable -> tmsTestCaseRepository.findIdsByCriteria(
                projectId,
                null, // no search query
                null, // no folder filter
                testPlanId, // filter by test plan
                pageable
            )
        );
  }

  /**
   * Verifies that test case is added to the test plan.
   *
   * @param testPlanId the test plan ID
   * @param testCaseId the test case ID
   * @param projectId  the project ID (for error message)
   * @throws ReportPortalException if test case not found in test plan
   */
  private void verifyTestCaseInTestPlan(Long testPlanId, Long testCaseId, Long projectId) {
    var testCaseIdsInPlan = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId);

    if (!testCaseIdsInPlan.contains(testCaseId)) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_CASE_NOT_FOUND_IN_TEST_PLAN.formatted(testCaseId, projectId, testPlanId)
      );
    }
  }

  private Filter enhanceFilterWithProjectIdAndTestPlanId(Filter originalFilter,
      Long projectId,
      Long testPlanId) {
    var conditions = new ArrayList<>(originalFilter.getFilterConditions());

    var hasProjectIdFilter = conditions
        .stream()
        .anyMatch(condition -> condition instanceof FilterCondition &&
            ((FilterCondition) condition).getSearchCriteria()
                .equals(CRITERIA_TMS_TEST_CASE_PROJECT_ID));

    if (!hasProjectIdFilter) {
      conditions.add(new FilterCondition(
          Condition.EQUALS,
          false,
          String.valueOf(projectId),
          CRITERIA_TMS_TEST_CASE_PROJECT_ID
      ));
    }

    var hasTestPlanIdIdFilter = conditions
        .stream()
        .anyMatch(condition -> condition instanceof FilterCondition &&
            ((FilterCondition) condition).getSearchCriteria()
                .equals(CRITERIA_TMS_TEST_CASE_PLAN_ID));

    if (!hasTestPlanIdIdFilter) {
      conditions.add(new FilterCondition(
          Condition.EQUALS,
          false,
          String.valueOf(testPlanId),
          CRITERIA_TMS_TEST_CASE_PLAN_ID
      ));
    }

    return new Filter(TmsTestCase.class, conditions);
  }
}
