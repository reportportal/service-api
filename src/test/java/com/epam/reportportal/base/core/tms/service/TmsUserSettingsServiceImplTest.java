package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsUserSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsUserSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsUserSettingsId;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class TmsUserSettingsServiceImplTest {

  private static final long PROJECT_ID = 1L;
  private static final long USER_ID = 2L;
  private static final TmsUserSettingsId ID = new TmsUserSettingsId(USER_ID, PROJECT_ID);

  @Mock
  private TmsUserSettingsRepository tmsUserSettingsRepository;

  private TmsUserSettingsServiceImpl sut;

  @BeforeEach
  void setUp() {
    sut = new TmsUserSettingsServiceImpl(tmsUserSettingsRepository, JsonMapper.builder().build());
  }

  @Test
  void get_WhenNothingStored_ShouldReturnEmptySettings() {
    when(tmsUserSettingsRepository.findById(ID)).thenReturn(Optional.empty());

    var result = sut.get(PROJECT_ID, USER_ID);

    assertTrue(result.settings().isEmpty());
  }

  @Test
  void get_WhenStored_ShouldReturnStoredSettings() {
    when(tmsUserSettingsRepository.findById(ID)).thenReturn(Optional.of(
        new TmsUserSettings(ID, Map.of("testCaseListPageSize", 50), LocalDateTime.now())));

    var result = sut.get(PROJECT_ID, USER_ID);

    assertEquals(Map.of("testCaseListPageSize", 50), result.settings());
  }

  @Test
  void update_ShouldSendSetValuesAndRemovedKeysSeparately() {
    var request = new HashMap<String, Object>();
    request.put("testCaseListPageSize", 50);
    request.put("testCaseListSorting", Map.of("field", "name", "direction", "ASC"));
    request.put("tmsFolderTreeWidth", null);
    when(tmsUserSettingsRepository.findById(ID)).thenReturn(Optional.of(
        new TmsUserSettings(ID, Map.of("testCaseListPageSize", 50), LocalDateTime.now())));

    var result = sut.update(PROJECT_ID, USER_ID, new TmsUserSettingsRQ(request));

    var mapper = JsonMapper.builder().build();
    var values = new HashMap<String, Object>(request);
    values.remove("tmsFolderTreeWidth");
    verify(tmsUserSettingsRepository).upsertSettings(
        eq(USER_ID),
        eq(PROJECT_ID),
        argThat(json -> mapper.readValue(json, Map.class)
            .equals(values)),
        eq("[\"tmsFolderTreeWidth\"]"));
    assertEquals(Map.of("testCaseListPageSize", 50), result.settings());
  }

  @Test
  void update_WhenValueTooLong_ShouldThrowBadRequestAndNotWrite() {
    var request = new TmsUserSettingsRQ(
        Map.of("key", "x".repeat(TmsUserSettingsServiceImpl.MAX_VALUE_LENGTH)));

    assertThrows(ReportPortalException.class, () -> sut.update(PROJECT_ID, USER_ID, request));

    verify(tmsUserSettingsRepository, never())
        .upsertSettings(anyLong(), anyLong(), anyString(), anyString());
  }

  @Test
  void update_WhenTooManyKeysStored_ShouldThrowBadRequest() {
    Map<String, Object> stored = IntStream
        .rangeClosed(0, TmsUserSettingsServiceImpl.MAX_STORED_KEYS)
        .boxed()
        .collect(Collectors.toMap(i -> "key" + i, i -> i));
    when(tmsUserSettingsRepository.findById(ID)).thenReturn(Optional.of(
        new TmsUserSettings(ID, stored, LocalDateTime.now())));

    assertThrows(ReportPortalException.class,
        () -> sut.update(PROJECT_ID, USER_ID, new TmsUserSettingsRQ(Map.of("key0", 1))));
  }
}
