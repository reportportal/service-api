# Quick Reference - Manual Test Case Executions Implementation

## 📍 Quick Navigation

### New Files Location
- **Entities**: `src/main/java/com/epam/reportportal/infrastructure/persistence/entity/tms/`
- **Repositories**: `src/main/java/com/epam/reportportal/infrastructure/persistence/dao/`
- **Services**: `src/main/java/com/epam/reportportal/core/tms/service/`
- **Mappers**: `src/main/java/com/epam/reportportal/core/tms/mapper/`
- **Migrations**: `src/main/resources/db/migrations/`

---

## 🔑 Key Services

### TmsTestCaseExecutionServiceImpl
**Orchestrator service** - delegates to specialized services

```java
// Entry point
addTestCasesToLaunch(projectId, launch, testCaseIds)
  └─ createExecution(projectId, testCase, launch)
```

### SuiteItemService
**SUITE item management**

```java
findOrCreateSuiteItem(projectId, testFolderId, launch)
markAsHavingChildren(suiteItem)
removeFolderTestItemLink(testItemId)
```

### TestCaseItemService
**TEST item creation with attributes**

```java
createTestCaseItem(name, description, attributes, suiteItem, launch)
markAsHavingNestedChildren(testItem)
```

### NestedStepsService
**Nested step creation**

```java
createNestedStepsFromStepScenario(stepsScenario, parentTestItem, launch)
createNestedStepFromTextScenario(textScenario, parentTestItem, launch)
```

### StepExecutionService
**Execution tracking**

```java
createStepExecutionRecords(testCaseExecutionId, nestedSteps, launch, tmsStepIds)
deleteStepExecutionsByTestCaseExecution(testCaseExecutionId)
```

---

## 🏗️ Entity Structure

### TmsTestFolderTestItem
```
test_folder_id (FK) → tms_test_folder.id
test_item_id (FK) → test_item.item_id
created_at (audited)
```

### TmsStepExecution
```
test_case_execution_id (FK) → tms_test_case_execution.id
test_item_id (FK) → test_item.item_id
launch_id (FK) → launch.id
project_id
tms_step_id (non-FK, tracking only)
created_at (audited)
updated_at (audited)
```

---

## 📊 Creation Flow

```
1. POST /tms/launch/{launchId}/test-case
2. TmsManualLaunchService.addTestCaseToLaunch()
3. TmsTestCaseExecutionServiceImpl.createExecution()
   ├─ SuiteItemService.findOrCreateSuiteItem()
   ├─ TestCaseItemService.createTestCaseItem()
   ├─ NestedStepsService.createNestedSteps*()
   ├─ Create TmsTestCaseExecution record
   └─ StepExecutionService.createStepExecutionRecords()
```

---

## 🎯 Test Item Hierarchy

```
SUITE (type=SUITE, hasStats=true, status=INFO)
  └─ TEST (type=TEST, hasStats=true, status=TO_RUN)
      ├─ Nested Step 1 (type=STEP, hasStats=false)
      ├─ Nested Step 2 (type=STEP, hasStats=false)
      └─ Nested Step N (type=STEP, hasStats=false)
```

---

## 📋 Scenario Types

### Steps-Based (TmsStepsManualScenarioRS)
- Multiple nested items
- Name: "Step 1: Open application", "Step 2: Click login", etc.
- Description: Expected result for each step

### Text-Based (TmsTextManualScenarioRS)
- Single nested item
- Name: Instructions text
- Description: Instructions + Expected result combined

---

## 🗄️ Database Queries

### Find SUITE by folder in launch
```java
testItemRepository.findSuiteItemInLaunchForFolder(launchId, testFolderId)
```

### Find nested steps under parent
```java
testItemRepository.findNestedStepsByParentId(parentId)
```

### Find step executions for test case
```java
stepExecutionService.getStepExecutionsByTestCaseExecution(testCaseExecutionId)
```

### Count TEST children under SUITE
```java
testItemRepository.countByParentIdAndType(suiteItemId, TestItemTypeEnum.TEST)
```

---

## ⚙️ Configuration

### Entity Auditing
Uses Spring Data auditing - ensure this is enabled:

```java
@Configuration
@EnableJpaAuditing
public class AuditingConfiguration {
}
```

### Transactional Boundaries
All service methods use `@Transactional`:
- Write operations: `@Transactional`
- Read-only operations: `@Transactional(readOnly = true)`

---

## 🐛 Debugging Tips

### Enable Detailed Logging
```properties
logging.level.com.epam.reportportal.core.tms=DEBUG
logging.level.com.epam.reportportal.core.tms.service=DEBUG
logging.level.com.epam.reportportal.infrastructure.persistence.dao=DEBUG
```

### Check Path Construction
- SUITE path: `{launchId}` (e.g., "123")
- TEST path: `{launchId}.{suiteId}.{testId}` (e.g., "123.456.789")
- Nested path: `{launchId}.{suiteId}.{testId}.{nestedId}` (e.g., "123.456.789.101")

### Verify Junction Records
```sql
SELECT * FROM tms_test_folder_test_item WHERE test_folder_id = ?;
SELECT * FROM tms_step_execution WHERE test_case_execution_id = ?;
```

---

## ✅ Validation Checklist

Before deploying:

- [ ] All services injected properly
- [ ] Database migrations applied
- [ ] Junction table populated (if needed)
- [ ] Path construction verified
- [ ] Attributes properly associated
- [ ] Nested steps created correctly
- [ ] Deletion cascade working
- [ ] Timestamps audited properly
- [ ] Logging shows expected flow
- [ ] No null pointer exceptions

---

## 🔄 Removal Flow

```
DELETE /tms/launch/{launchId}/test-case/execution/{executionId}
  1. Delete TmsStepExecution records
  2. Delete TEST item (cascade → nested steps)
  3. Delete TmsTestCaseExecution record
  4. Check SUITE item:
     - If no TEST children remain: delete SUITE
     - Else: keep SUITE for other tests
```

---

## 📈 Performance Considerations

### Indexes Created
- `idx_tms_test_folder_test_item_folder` - query SUITE by folder
- `idx_tms_test_folder_test_item_item` - query folder by item
- `idx_tms_step_execution_test_case` - query steps by execution
- `idx_tms_step_execution_test_item` - query execution by step
- `idx_tms_step_execution_launch` - query steps by launch
- `idx_tms_step_execution_tms_step` - query by original step ID

### Query Optimization
- Use junction queries for SUITE lookup (indexed)
- Use path-based queries for nested items (LTREE)
- Batch create nested steps in single transaction
- Limit batch size for large test folders

---

## 🚨 Known Constraints

1. **Max Path Depth**: 64 segments (enforced by PathLengthValidator)
2. **Nested Step Parent**: Must be stats-aware item (hasStats=true)
3. **SUITE Status**: Always INFO (not computed from children)
4. **TEST Attributes**: Only on TEST items, not SUITE or nested items
5. **Nested Steps**: Cannot have issues assigned (no issue resolution)

---

## 📞 Support References

- **PR**: https://github.com/reportportal/service-api/pull/2478
- **Branch**: `feature/EPMRPP-manual-launches-test-folders-to-migrate-tms-to-develop`
- **Base**: `feature/EPMRPP-manual-launches-to-migrate-tms-to-develop`
- **Implementation Doc**: `IMPLEMENTATION_COMPLETE.md`

---

**Last Updated**: 2024
**Status**: ✅ Ready for Use
