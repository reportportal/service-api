/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.ProjectLogType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class LogTypeRepositoryTest extends BaseMvcTest {

  @Autowired
  private LogTypeRepository logTypeRepository;

  @Autowired
  private CacheManager cacheManager;

  @Test
  void findByProjectIdWhenProjectWithDefaultLogTypesExistsShouldReturnAllDefaultTypesWithExpectedValues() {
    // given
    final long projectId = 1L;
    final List<ProjectLogType> expectedDefaultLogTypes = List.of(
        new ProjectLogType(null, projectId, "unknown", 60000, "#E3E7EC", "#FFFFFF", "#464547",
            "normal", false, true, null, null),
        new ProjectLogType(null, projectId, "fatal", 50000, "#8B0000", "#FFFFFF", "#464547",
            "normal", true, true, null, null),
        new ProjectLogType(null, projectId, "error", 40000, "#DC5959", "#FFFFFF", "#464547",
            "normal", true, true, null, null),
        new ProjectLogType(null, projectId, "warn", 30000, "#FFBC6C", "#FFFFFF", "#464547",
            "normal", true, true, null, null),
        new ProjectLogType(null, projectId, "info", 20000, "#23A6DE", "#FFFFFF", "#464547",
            "normal", true, true, null, null),
        new ProjectLogType(null, projectId, "debug", 10000, "#C1C7D0", "#FFFFFF", "#464547",
            "normal", true, true, null, null),
        new ProjectLogType(null, projectId, "trace", 5000, "#E3E7EC", "#FFFFFF", "#464547",
            "normal", true, true, null, null)
    );

    // when
    List<ProjectLogType> actualLogTypes = logTypeRepository.findByProjectId(projectId);

    // then
    assertEquals(7, actualLogTypes.size());
    for (int i = 0; i < actualLogTypes.size(); i++) {
      ProjectLogType expected = expectedDefaultLogTypes.get(i);
      ProjectLogType actual = actualLogTypes.get(i);
      assertNotNull(actual.getId());
      assertEquals(expected.getProjectId(), actual.getProjectId());
      assertEquals(expected.getName(), actual.getName());
      assertEquals(expected.getLevel(), actual.getLevel());
      assertEquals(expected.getLabelColor(), actual.getLabelColor());
      assertEquals(expected.getBackgroundColor(), actual.getBackgroundColor());
      assertEquals(expected.getTextColor(), actual.getTextColor());
      assertEquals(expected.getTextStyle(), actual.getTextStyle());
      assertEquals(expected.isFilterable(), actual.isFilterable());
      assertEquals(expected.isSystem(), actual.isSystem());
    }
  }

  @Test
  void findByProjectIdWhenProjectDoesNotExistShouldReturnEmptyList() {
    // given
    final long projectId = 1234L;

    // when
    List<ProjectLogType> logTypes = logTypeRepository.findByProjectId(projectId);

    // then
    assertTrue(logTypes.isEmpty());
  }

  @Test
  void findByProjectIdShouldPopulateCache() {
    // given
    final long projectId = 1L;

    // when
    List<ProjectLogType> result = logTypeRepository.findByProjectId(projectId);

    Cache cache = cacheManager.getCache("projectLogTypeCache");
    assertNotNull(cache);

    Cache.ValueWrapper cachedValue = cache.get(projectId);
    assertNotNull(cachedValue);

    List<ProjectLogType> cachedList = (List<ProjectLogType>) cachedValue.get();
    assertNotNull(cachedList);
    assertEquals(7, cachedList.size());

    // then
    assertEquals(result.size(), cachedList.size());
    for (int i = 0; i < result.size(); i++) {
      assertEquals(result.get(i).getName(), cachedList.get(i).getName());
      assertEquals(result.get(i).getLevel(), cachedList.get(i).getLevel());
    }
  }

  @Test
  void existsByProjectIdAndNameOrLevelWhenDuplicateByNameExistsShouldReturnTrue() {
    // given
    final long projectId = 1L;
    final String existingName = "Info";
    final int newLevel = 223445;

    // when
    boolean exists = logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(projectId,
        existingName, newLevel);

    // then
    assertTrue(exists);
  }

  @Test
  void existsByProjectIdAndNameOrLevelWhenDuplicateByLevelExistsShouldReturnTrue() {
    // given
    final long projectId = 1L;
    final String newName = "New name";
    final int existingLevel = 10000;

    // when
    boolean exists = logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(projectId, newName,
        existingLevel);

    // then
    assertTrue(exists);
  }

  @Test
  void existsByProjectIdAndNameOrLevelWhenNoDuplicateExistsShouldReturnFalse() {
    // given
    final long projectId = 1L;
    final String newName = "New name";
    final int newLevel = 1234556;

    // when
    boolean exists = logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(projectId, newName,
        newLevel);

    // then
    assertFalse(exists);
  }

  @Test
  void countFilterableLogTypesWhenProjectWithDefaultLogTypesShouldReturnSix() {
    // given
    final long projectId = 1L;

    // when
    long logTypesCount = logTypeRepository.countFilterableLogTypes(projectId);

    // then
    assertEquals(6, logTypesCount);
  }

  @Test
  void findLevelByProjectIdAndNameWhenLogTypeExistsShouldReturnTraceLevel() {
    // given
    final long projectId = 1L;
    final String levelName = "Trace";

    // when
    Optional<Integer> level = logTypeRepository.findLevelByProjectIdAndNameIgnoreCase(projectId,
        levelName);

    Cache cache = cacheManager.getCache("projectLogTypeWithLevelNameCache");
    assertNotNull(cache);
    Cache.ValueWrapper valueWrapper = cache.get(projectId + "_" + levelName);
    assertNotNull(valueWrapper);

    // then
    assertTrue(level.isPresent());
    assertEquals(5000, level.get());
  }

  @Test
  void findLevelByProjectIdAndNameWhenLogTypeDoesNotExistShouldReturnEmptyOptional() {
    // given
    final long projectId = 1L;
    final String nonExistingLevelName = "custom-error";

    // when
    Optional<Integer> level = logTypeRepository.findLevelByProjectIdAndNameIgnoreCase(
        projectId, nonExistingLevelName);

    // then
    assertFalse(level.isPresent());
  }

  @Test
  void findNameByProjectIdAndLevelWhenLevelExistsShouldReturnName() {
    // given
    final long projectId = 1L;
    final int level = 20000;

    // when
    String levelName = logTypeRepository.findNameByProjectIdAndLevel(projectId, level);

    Cache cache = cacheManager.getCache("projectLogTypeWithLevelCache");
    assertNotNull(cache);
    Cache.ValueWrapper valueWrapper = cache.get(projectId + "_" + level);
    assertNotNull(valueWrapper);
    assertEquals("info", valueWrapper.get().toString());

    // then
    assertEquals("info", levelName);
  }

  @Test
  void findNameByProjectIdAndLevelWhenLevelDoesNotExistShouldReturnNull() {
    // given
    final long projectId = 1L;
    final int nonExistingLevel = 12345;

    // when
    String levelName = logTypeRepository.findNameByProjectIdAndLevel(projectId, nonExistingLevel);

    // then
    assertNull(levelName);
  }

  @Test
  void existsByProjectIdAndNameOrLevelExcludingIdWhenDuplicateByNameExistsInOtherLogTypeShouldReturnTrue() {
    // given
    final long projectId = 1L;
    final String existingName = "Info";
    final int newLevel = 223445;
    final long excludeId = 1L;

    // when
    boolean exists = logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCaseExcludingId(
        projectId, existingName, newLevel, excludeId);

    // then
    assertTrue(exists);
  }

  @Test
  void existsByProjectIdAndNameOrLevelExcludingIdWhenMatchingNameBelongsToExcludedIdShouldReturnFalse() {
    // given
    final long projectId = 1L;
    List<ProjectLogType> logTypes = logTypeRepository.findByProjectId(projectId);
    ProjectLogType infoLogType = logTypes.stream()
        .filter(lt -> "info".equals(lt.getName()))
        .findFirst()
        .orElseThrow();
    final String existingName = "Info";
    final int newLevel = 999999;
    final long excludeId = infoLogType.getId();

    // when
    boolean exists = logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCaseExcludingId(
        projectId, existingName, newLevel, excludeId);

    // then
    assertFalse(exists);
  }

  @Test
  void existsByProjectIdAndNameOrLevelExcludingIdWhenMatchingLevelBelongsToExcludedIdShouldReturnFalse() {
    // given
    final long projectId = 1L;
    List<ProjectLogType> logTypes = logTypeRepository.findByProjectId(projectId);
    ProjectLogType debugLogType = logTypes.stream()
        .filter(lt -> "debug".equals(lt.getName()))
        .findFirst()
        .orElseThrow();
    final String newName = "New name";
    final int existingLevel = 10000;
    final long excludeId = debugLogType.getId();

    // when
    boolean exists = logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCaseExcludingId(
        projectId, newName, existingLevel, excludeId);

    // then
    assertFalse(exists);
  }

  @Test
  void saveLogTypeShouldEvictAllRelatedCaches() {
    // given
    final long projectId = 1L;
    logTypeRepository.findByProjectId(projectId);
    logTypeRepository.findLevelByProjectIdAndNameIgnoreCase(projectId, "info");
    logTypeRepository.findNameByProjectIdAndLevel(projectId, 20000);

    Cache projectLogTypeCache = cacheManager.getCache("projectLogTypeCache");
    Cache levelNameCache = cacheManager.getCache("projectLogTypeWithLevelNameCache");
    Cache levelCache = cacheManager.getCache("projectLogTypeWithLevelCache");

    assertNotNull(projectLogTypeCache.get(projectId));
    assertNotNull(levelNameCache.get(projectId + "_info"));
    assertNotNull(levelCache.get(projectId + "_20000"));

    ProjectLogType logType = new ProjectLogType();
    logType.setProjectId(projectId);
    logType.setName("custom");
    logType.setLevel(25000);
    logType.setLabelColor("#FF0000");
    logType.setBackgroundColor("#FFFFFF");
    logType.setTextColor("#000000");
    logType.setTextStyle("normal");
    logType.setFilterable(false);
    logType.setSystem(false);

    // when
    logTypeRepository.save(logType);

    // then
    assertNull(projectLogTypeCache.get(projectId));
    assertNull(levelNameCache.get(projectId + "_info"));
    assertNull(levelCache.get(projectId + "_20000"));
  }

  @Test
  void deleteLogTypeShouldEvictAllRelatedCaches() {
    // Given
    final long projectId = 1L;
    ProjectLogType logType = new ProjectLogType();
    logType.setProjectId(projectId);
    logType.setName("custom");
    logType.setLevel(25000);
    logType.setLabelColor("#FF0000");
    logType.setBackgroundColor("#FFFFFF");
    logType.setTextColor("#000000");
    logType.setTextStyle("normal");
    logType.setFilterable(false);
    logType.setSystem(false);
    ProjectLogType savedLogType = logTypeRepository.save(logType);

    logTypeRepository.findByProjectId(projectId);
    logTypeRepository.findLevelByProjectIdAndNameIgnoreCase(projectId, "info");
    logTypeRepository.findNameByProjectIdAndLevel(projectId, 20000);

    Cache projectLogTypeCache = cacheManager.getCache("projectLogTypeCache");
    Cache levelNameCache = cacheManager.getCache("projectLogTypeWithLevelNameCache");
    Cache levelCache = cacheManager.getCache("projectLogTypeWithLevelCache");

    assertNotNull(projectLogTypeCache.get(projectId));
    assertNotNull(levelNameCache.get(projectId + "_info"));
    assertNotNull(levelCache.get(projectId + "_20000"));

    // When
    logTypeRepository.delete(savedLogType);

    // Then
    assertNull(projectLogTypeCache.get(projectId));
    assertNull(levelNameCache.get(projectId + "_info"));
    assertNull(levelCache.get(projectId + "_20000"));
  }
}
