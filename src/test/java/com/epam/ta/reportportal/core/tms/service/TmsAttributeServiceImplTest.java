package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.db.repository.TmsAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsAttributeMapper;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TmsAttributeServiceImpl Tests")
class TmsAttributeServiceImplTest {

  @Mock
  private TmsAttributeRepository tmsAttributeRepository;

  @Mock
  private TmsAttributeMapper tmsAttributeMapper;

  private TmsAttributeServiceImpl tmsAttributeService;

  @BeforeEach
  void setUp() {
    tmsAttributeService = new TmsAttributeServiceImpl(tmsAttributeRepository, tmsAttributeMapper);
  }

  @Test
  @DisplayName("Should find existing attributes by id")
  void shouldFindExistingAttributesById() {
    // given
    var attribute1 = createTmsAttribute(1L, "key1");
    var attribute2 = createTmsAttribute(2L, "key2");

    var request1 = createTmsAttributeRQ(1L, null, "value1");
    var request2 = createTmsAttributeRQ(2L, null, "value2");
    var requests = List.of(request1, request2);

    when(tmsAttributeRepository.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(attribute1, attribute2));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(2, result.size());
    assertEquals(attribute1, result.get("id:1"));
    assertEquals(attribute2, result.get("id:2"));

    verify(tmsAttributeRepository).findAllById(List.of(1L, 2L));
    verify(tmsAttributeRepository, never()).findAllByKeyIn(anyList());
    verify(tmsAttributeRepository, never()).saveAll(anyList());
    verify(tmsAttributeMapper, never()).createTmsAttribute(any());
  }

  @Test
  @DisplayName("Should create new attributes by key when key does not exist")
  void shouldCreateNewAttributesByKeyWhenKeyDoesNotExist() {
    // given
    var newAttribute1 = createTmsAttribute(3L, "new-key1");
    var newAttribute2 = createTmsAttribute(4L, "new-key2");

    var request1 = createTmsAttributeRQ(null, "new-key1", "value1");
    var request2 = createTmsAttributeRQ(null, "new-key2", "value2");
    var requests = List.of(request1, request2);

    var mappedAttribute1 = createTmsAttribute(null, "new-key1");
    var mappedAttribute2 = createTmsAttribute(null, "new-key2");

    when(tmsAttributeRepository.findAllByKeyIn(List.of("new-key1", "new-key2")))
        .thenReturn(Collections.emptyList());
    when(tmsAttributeMapper.createTmsAttribute("new-key1")).thenReturn(mappedAttribute1);
    when(tmsAttributeMapper.createTmsAttribute("new-key2")).thenReturn(mappedAttribute2);
    when(tmsAttributeRepository.saveAll(List.of(mappedAttribute1, mappedAttribute2)))
        .thenReturn(List.of(newAttribute1, newAttribute2));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(2, result.size());
    assertEquals(newAttribute1, result.get("key:new-key1"));
    assertEquals(newAttribute2, result.get("key:new-key2"));

    verify(tmsAttributeRepository, never()).findAllById(anyList());
    verify(tmsAttributeRepository).findAllByKeyIn(List.of("new-key1", "new-key2"));
    verify(tmsAttributeRepository).saveAll(List.of(mappedAttribute1, mappedAttribute2));
    verify(tmsAttributeMapper).createTmsAttribute("new-key1");
    verify(tmsAttributeMapper).createTmsAttribute("new-key2");
  }

  @Test
  @DisplayName("Should find existing attributes by key when key already exists")
  void shouldFindExistingAttributesByKeyWhenKeyAlreadyExists() {
    // given
    var existingAttribute1 = createTmsAttribute(1L, "existing-key1");
    var existingAttribute2 = createTmsAttribute(2L, "existing-key2");

    var request1 = createTmsAttributeRQ(null, "existing-key1", "value1");
    var request2 = createTmsAttributeRQ(null, "existing-key2", "value2");
    var requests = List.of(request1, request2);

    when(tmsAttributeRepository.findAllByKeyIn(List.of("existing-key1", "existing-key2")))
        .thenReturn(List.of(existingAttribute1, existingAttribute2));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(2, result.size());
    assertEquals(existingAttribute1, result.get("key:existing-key1"));
    assertEquals(existingAttribute2, result.get("key:existing-key2"));

    verify(tmsAttributeRepository, never()).findAllById(anyList());
    verify(tmsAttributeRepository).findAllByKeyIn(List.of("existing-key1", "existing-key2"));
    verify(tmsAttributeRepository, never()).saveAll(anyList());
    verify(tmsAttributeMapper, never()).createTmsAttribute(any());
  }

  @Test
  @DisplayName("Should handle mixed scenario: some keys exist, some need to be created")
  void shouldHandleMixedScenarioSomeKeysExistSomeNeedToBeCreated() {
    // given
    var existingAttribute = createTmsAttribute(1L, "existing-key");
    var newAttribute = createTmsAttribute(2L, "new-key");

    var request1 = createTmsAttributeRQ(null, "existing-key", "value1");
    var request2 = createTmsAttributeRQ(null, "new-key", "value2");
    var requests = List.of(request1, request2);

    var mappedAttribute = createTmsAttribute(null, "new-key");

    when(tmsAttributeRepository.findAllByKeyIn(List.of("existing-key", "new-key")))
        .thenReturn(List.of(existingAttribute)); // only existing-key found
    when(tmsAttributeMapper.createTmsAttribute("new-key")).thenReturn(mappedAttribute);
    when(tmsAttributeRepository.saveAll(List.of(mappedAttribute)))
        .thenReturn(List.of(newAttribute));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(2, result.size());
    assertEquals(existingAttribute, result.get("key:existing-key"));
    assertEquals(newAttribute, result.get("key:new-key"));

    verify(tmsAttributeRepository).findAllByKeyIn(List.of("existing-key", "new-key"));
    verify(tmsAttributeRepository).saveAll(List.of(mappedAttribute));
    verify(tmsAttributeMapper).createTmsAttribute("new-key");
  }

  @Test
  @DisplayName("Should handle mixed requests with both id and key")
  void shouldHandleMixedRequestsWithBothIdAndKey() {
    // given
    var existingAttribute = createTmsAttribute(1L, "existing-key");
    var newAttribute = createTmsAttribute(2L, "new-key");

    var request1 = createTmsAttributeRQ(1L, null, "value1");
    var request2 = createTmsAttributeRQ(null, "new-key", "value2");
    var requests = List.of(request1, request2);

    var mappedAttribute = createTmsAttribute(null, "new-key");

    when(tmsAttributeRepository.findAllById(List.of(1L)))
        .thenReturn(List.of(existingAttribute));
    when(tmsAttributeRepository.findAllByKeyIn(List.of("new-key")))
        .thenReturn(Collections.emptyList());
    when(tmsAttributeMapper.createTmsAttribute("new-key")).thenReturn(mappedAttribute);
    when(tmsAttributeRepository.saveAll(List.of(mappedAttribute)))
        .thenReturn(List.of(newAttribute));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(2, result.size());
    assertEquals(existingAttribute, result.get("id:1"));
    assertEquals(newAttribute, result.get("key:new-key"));

    verify(tmsAttributeRepository).findAllById(List.of(1L));
    verify(tmsAttributeRepository).findAllByKeyIn(List.of("new-key"));
    verify(tmsAttributeRepository).saveAll(List.of(mappedAttribute));
    verify(tmsAttributeMapper).createTmsAttribute("new-key");
  }

  @Test
  @DisplayName("Should deduplicate ids in requests")
  void shouldDeduplicateIdsInRequests() {
    // given
    var attribute = createTmsAttribute(1L, "key1");

    var request1 = createTmsAttributeRQ(1L, null, "value1");
    var request2 = createTmsAttributeRQ(1L, null, "value2");
    var requests = List.of(request1, request2);

    when(tmsAttributeRepository.findAllById(List.of(1L)))
        .thenReturn(List.of(attribute));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(1, result.size());
    assertEquals(attribute, result.get("id:1"));

    verify(tmsAttributeRepository).findAllById(List.of(1L));
    verify(tmsAttributeRepository, never()).findAllByKeyIn(anyList());
    verify(tmsAttributeRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("Should deduplicate keys in requests")
  void shouldDeduplicateKeysInRequests() {
    // given
    var newAttribute = createTmsAttribute(1L, "duplicate-key");

    var request1 = createTmsAttributeRQ(null, "duplicate-key", "value1");
    var request2 = createTmsAttributeRQ(null, "duplicate-key", "value2");
    var requests = List.of(request1, request2);

    var mappedAttribute = createTmsAttribute(null, "duplicate-key");

    when(tmsAttributeRepository.findAllByKeyIn(List.of("duplicate-key")))
        .thenReturn(Collections.emptyList());
    when(tmsAttributeMapper.createTmsAttribute("duplicate-key")).thenReturn(mappedAttribute);
    when(tmsAttributeRepository.saveAll(List.of(mappedAttribute)))
        .thenReturn(List.of(newAttribute));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(1, result.size());
    assertEquals(newAttribute, result.get("key:duplicate-key"));

    verify(tmsAttributeRepository).findAllByKeyIn(List.of("duplicate-key"));
    verify(tmsAttributeRepository).saveAll(List.of(mappedAttribute));
    verify(tmsAttributeMapper, times(1)).createTmsAttribute("duplicate-key");
  }

  @Test
  @DisplayName("Should skip invalid requests with null id and blank key")
  void shouldSkipInvalidRequestsWithNullIdAndBlankKey() {
    // given
    var validAttribute = createTmsAttribute(1L, "valid-key");

    var validRequest = createTmsAttributeRQ(1L, null, "value1");
    var invalidRequest1 = createTmsAttributeRQ(null, null, "value2");
    var invalidRequest2 = createTmsAttributeRQ(null, "", "value3");
    var invalidRequest3 = createTmsAttributeRQ(null, "   ", "value4");
    var requests = List.of(validRequest, invalidRequest1, invalidRequest2, invalidRequest3);

    when(tmsAttributeRepository.findAllById(List.of(1L)))
        .thenReturn(List.of(validAttribute));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(1, result.size());
    assertEquals(validAttribute, result.get("id:1"));

    verify(tmsAttributeRepository).findAllById(List.of(1L));
    verify(tmsAttributeRepository, never()).findAllByKeyIn(anyList());
    verify(tmsAttributeRepository, never()).saveAll(anyList());
    verify(tmsAttributeMapper, never()).createTmsAttribute(any());
  }

  @Test
  @DisplayName("Should return empty map when no valid requests")
  void shouldReturnEmptyMapWhenNoValidRequests() {
    // given
    var invalidRequest1 = createTmsAttributeRQ(null, null, "value1");
    var invalidRequest2 = createTmsAttributeRQ(null, "", "value2");
    var requests = List.of(invalidRequest1, invalidRequest2);

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertTrue(result.isEmpty());

    verify(tmsAttributeRepository, never()).findAllById(anyList());
    verify(tmsAttributeRepository, never()).findAllByKeyIn(anyList());
    verify(tmsAttributeRepository, never()).saveAll(anyList());
    verify(tmsAttributeMapper, never()).createTmsAttribute(any());
  }

  @Test
  @DisplayName("Should handle case when some ids not found in repository")
  void shouldHandleCaseWhenSomeIdsNotFoundInRepository() {
    // given
    var existingAttribute = createTmsAttribute(1L, "key1");

    var request1 = createTmsAttributeRQ(1L, null, "value1");
    var request2 = createTmsAttributeRQ(999L, null, "value2"); // non-existing id
    var requests = List.of(request1, request2);

    when(tmsAttributeRepository.findAllById(List.of(1L, 999L)))
        .thenReturn(List.of(existingAttribute)); // only one found

    // when
    var exception = assertThrows(ReportPortalException.class,
        () -> tmsAttributeService.getTmsAttributes(requests));

    // then
    verify(tmsAttributeRepository).findAllById(List.of(1L, 999L));

    AssertionsForClassTypes.assertThat(exception.getMessage()).contains("999");
  }

  @Test
  @DisplayName("Should handle empty ids list")
  void shouldHandleEmptyIdsList() {
    // given
    var newAttribute = createTmsAttribute(1L, "new-key");

    var request = createTmsAttributeRQ(null, "new-key", "value");
    var requests = List.of(request);

    var mappedAttribute = createTmsAttribute(null, "new-key");

    when(tmsAttributeRepository.findAllByKeyIn(List.of("new-key")))
        .thenReturn(Collections.emptyList());
    when(tmsAttributeMapper.createTmsAttribute("new-key")).thenReturn(mappedAttribute);
    when(tmsAttributeRepository.saveAll(List.of(mappedAttribute)))
        .thenReturn(List.of(newAttribute));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(1, result.size());
    assertEquals(newAttribute, result.get("key:new-key"));

    verify(tmsAttributeRepository, never()).findAllById(anyList());
    verify(tmsAttributeRepository).findAllByKeyIn(List.of("new-key"));
    verify(tmsAttributeRepository).saveAll(List.of(mappedAttribute));
  }

  @Test
  @DisplayName("Should handle empty keys list")
  void shouldHandleEmptyKeysList() {
    // given
    var existingAttribute = createTmsAttribute(1L, "key1");

    var request = createTmsAttributeRQ(1L, null, "value");
    var requests = List.of(request);

    when(tmsAttributeRepository.findAllById(List.of(1L)))
        .thenReturn(List.of(existingAttribute));

    // when
    var result = tmsAttributeService.getTmsAttributes(requests);

    // then
    assertEquals(1, result.size());
    assertEquals(existingAttribute, result.get("id:1"));

    verify(tmsAttributeRepository).findAllById(List.of(1L));
    verify(tmsAttributeRepository, never()).findAllByKeyIn(anyList());
    verify(tmsAttributeRepository, never()).saveAll(anyList());
    verify(tmsAttributeMapper, never()).createTmsAttribute(any());
  }

  @Test
  @DisplayName("Should throw exception when some ids not found in repository")
  void shouldThrowExceptionWhenSomeIdsNotFoundInRepository() {
    // given
    var existingAttribute = createTmsAttribute(1L, "key1");

    var request1 = createTmsAttributeRQ(1L, null, "value1");
    var request2 = createTmsAttributeRQ(999L, null, "value2"); // non-existing id
    var requests = List.of(request1, request2);

    when(tmsAttributeRepository.findAllById(List.of(1L, 999L)))
        .thenReturn(List.of(existingAttribute)); // only one found

    // when & then
    var exception = assertThrows(ReportPortalException.class,
        () -> tmsAttributeService.getTmsAttributes(requests));

    assertThat(exception.getMessage()).contains("999");
    verify(tmsAttributeRepository).findAllById(List.of(1L, 999L));
  }

  @Test
  @DisplayName("Should throw exception when multiple ids not found")
  void shouldThrowExceptionWhenMultipleIdsNotFound() {
    // given
    var request1 = createTmsAttributeRQ(1L, null, "value1");
    var request2 = createTmsAttributeRQ(999L, null, "value2");
    var request3 = createTmsAttributeRQ(888L, null, "value3");
    var requests = List.of(request1, request2, request3);

    when(tmsAttributeRepository.findAllById(List.of(1L, 999L, 888L)))
        .thenReturn(List.of()); // none found

    // when & then
    var exception = assertThrows(ReportPortalException.class,
        () -> tmsAttributeService.getTmsAttributes(requests));

    assertThat(exception.getMessage()).contains("1", "999", "888");
  }

  private TmsAttribute createTmsAttribute(Long id, String key) {
    var attribute = new TmsAttribute();
    attribute.setId(id);
    attribute.setKey(key);
    return attribute;
  }

  private TmsAttributeRQ createTmsAttributeRQ(Long id, String key, String value) {
    var request = new TmsAttributeRQ();
    request.setId(id);
    request.setKey(key);
    request.setValue(value);
    return request;
  }
}
