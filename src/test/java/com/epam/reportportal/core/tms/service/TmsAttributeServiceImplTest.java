package com.epam.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.core.tms.mapper.TmsAttributeMapper;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.filterable.TmsAttributeFilterableRepository;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import jakarta.persistence.EntityExistsException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TmsAttributeServiceImplTest {

  private static final Long PROJECT_ID = 1L;

  @Mock
  private TmsAttributeRepository tmsAttributeRepository;

  @Mock
  private TmsAttributeFilterableRepository tmsAttributeFilterableRepository;

  @Mock
  private TmsAttributeMapper tmsAttributeMapper;

  @InjectMocks
  private TmsAttributeServiceImpl tmsAttributeService;

  private TmsAttributeRQ attributeRequest;
  private TmsAttribute attributeEntity;
  private TmsAttributeRS attributeResponse;
  private Pageable pageable;
  private Filter filter;

  @BeforeEach
  void setUp() {
    attributeRequest = createAttributeRequest();
    attributeEntity = createAttributeEntity();
    attributeResponse = createAttributeResponse();
    pageable = PageRequest.of(0, 10);
    filter = mock(Filter.class);
  }

  @Test
  void shouldCreateAttribute() {
    // Given
    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID,
        attributeRequest.getKey(), attributeRequest.getValue())).thenReturn(false);
    when(tmsAttributeMapper.convertToTmsAttribute(attributeRequest, PROJECT_ID)).thenReturn(
        attributeEntity);
    when(tmsAttributeRepository.save(attributeEntity)).thenReturn(attributeEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attributeEntity)).thenReturn(attributeResponse);

    // When
    var result = tmsAttributeService.create(PROJECT_ID, attributeRequest);

    // Then
    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID,
        attributeRequest.getKey(), attributeRequest.getValue());
    verify(tmsAttributeMapper).convertToTmsAttribute(attributeRequest, PROJECT_ID);
    verify(tmsAttributeRepository).save(any(TmsAttribute.class));
    verify(tmsAttributeMapper).convertToTmsAttributeRS(attributeEntity);

    assertThat(result).isEqualTo(attributeResponse);
  }

  @Test
  void shouldThrowEntityExistsExceptionWhenKeyAndValueAlreadyExistInProject() {
    // Given
    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID,
        attributeRequest.getKey(), attributeRequest.getValue())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.create(PROJECT_ID, attributeRequest))
        .isInstanceOf(EntityExistsException.class)
        .hasMessageContaining("TMS Attribute with key '" + attributeRequest.getKey()
            + "' and value '" + attributeRequest.getValue()
            + "' already exists in project '" + PROJECT_ID + "'");

    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID,
        attributeRequest.getKey(), attributeRequest.getValue());
    verify(tmsAttributeMapper, never()).convertToTmsAttribute(any(), eq(PROJECT_ID));
    verify(tmsAttributeRepository, never()).save(any());
    verify(tmsAttributeMapper, never()).convertToTmsAttributeRS(any());
  }

  @Test
  void shouldPatchAttributeWhenKeyAndValueChanged() {
    // Given
    var attributeId = 1L;
    var updatedRequest = createUpdatedAttributeRequest();
    var existingEntity = createAttributeEntity();
    var updatedEntity = createUpdatedAttributeEntity();
    var updatedResponse = createUpdatedAttributeResponse();

    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue())).thenReturn(false);
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity)).thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(PROJECT_ID, attributeId, updatedRequest);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue());
    verify(tmsAttributeMapper).patch(existingEntity, updatedRequest);
    verify(tmsAttributeRepository).save(existingEntity);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(updatedEntity);

    assertThat(result).isEqualTo(updatedResponse);
  }

  @Test
  void shouldPatchAttributeWhenOnlyKeyChanged() {
    // Given
    var attributeId = 1L;
    var updatedRequest = TmsAttributeRQ.builder()
        .key("new-key")
        .value("test-value")
        .build();
    var existingEntity = createAttributeEntity();
    var updatedEntity = createUpdatedAttributeEntity();
    var updatedResponse = createUpdatedAttributeResponse();

    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue())).thenReturn(false);
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity)).thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(PROJECT_ID, attributeId, updatedRequest);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue());
    verify(tmsAttributeMapper).patch(existingEntity, updatedRequest);
    verify(tmsAttributeRepository).save(existingEntity);

    assertThat(result).isEqualTo(updatedResponse);
  }

  @Test
  void shouldPatchAttributeWhenOnlyValueChanged() {
    // Given
    var attributeId = 1L;
    var updatedRequest = TmsAttributeRQ.builder()
        .key("test-key")
        .value("new-value")
        .build();
    var existingEntity = createAttributeEntity();
    var updatedEntity = createUpdatedAttributeEntity();
    var updatedResponse = createUpdatedAttributeResponse();

    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue())).thenReturn(false);
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity)).thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(PROJECT_ID, attributeId, updatedRequest);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue());
    verify(tmsAttributeMapper).patch(existingEntity, updatedRequest);
    verify(tmsAttributeRepository).save(existingEntity);

    assertThat(result).isEqualTo(updatedResponse);
  }

  @Test
  void shouldPatchAttributeWhenNothingChanged() {
    // Given
    var attributeId = 1L;
    var updatedRequest = TmsAttributeRQ.builder()
        .key("test-key")
        .value("test-value")
        .build();
    var existingEntity = createAttributeEntity();
    var updatedEntity = createAttributeEntity();
    var updatedResponse = createAttributeResponse();

    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(existingEntity));
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity)).thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(PROJECT_ID, attributeId, updatedRequest);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeRepository, never()).existsByProjectIdAndKeyAndValue(any(), any(), any());
    verify(tmsAttributeMapper).patch(existingEntity, updatedRequest);
    verify(tmsAttributeRepository).save(existingEntity);

    assertThat(result).isEqualTo(updatedResponse);
  }

  @Test
  void shouldThrowExceptionWhenPatchingToExistingKeyAndValue() {
    // Given
    var attributeId = 1L;
    var updatedRequest = createUpdatedAttributeRequest();
    var existingEntity = createAttributeEntity();

    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.patch(PROJECT_ID, attributeId, updatedRequest))
        .isInstanceOf(EntityExistsException.class)
        .hasMessageContaining("TMS Attribute with key '" + updatedRequest.getKey()
            + "' and value '" + updatedRequest.getValue()
            + "' already exists in project '" + PROJECT_ID + "'");

    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID,
        updatedRequest.getKey(), updatedRequest.getValue());
    verify(tmsAttributeMapper, never()).patch(any(), any());
    verify(tmsAttributeRepository, never()).save(any());
  }

  @Test
  void shouldGetAllWithFilter() {
    // Given
    var attributes = List.of(attributeEntity);
    var page = new PageImpl<>(attributes, pageable, attributes.size());

    when(tmsAttributeFilterableRepository.findByProjectIdAndFilter(eq(PROJECT_ID), any(),
        eq(pageable))).thenReturn(page);

    // When
    var result = tmsAttributeService.getAll(PROJECT_ID, filter, pageable);

    // Then
    verify(tmsAttributeFilterableRepository).findByProjectIdAndFilter(eq(PROJECT_ID), any(),
        eq(pageable));
    assertThat(result).isNotNull();
  }

  @Test
  void shouldGetAttributeById() {
    // Given
    var attributeId = 1L;
    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(attributeEntity));
    when(tmsAttributeMapper.convertToTmsAttributeRS(attributeEntity)).thenReturn(attributeResponse);

    // When
    var result = tmsAttributeService.getById(PROJECT_ID, attributeId);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(attributeEntity);
    assertThat(result).isEqualTo(attributeResponse);
  }

  @Test
  void shouldThrowExceptionWhenAttributeNotFoundById() {
    // Given
    var attributeId = 1L;
    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.empty());

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.getById(PROJECT_ID, attributeId))
        .isInstanceOf(ReportPortalException.class)
        .hasMessageContaining("TMS Attribute with id '" + attributeId + "' in project '" + PROJECT_ID + "'");

    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeMapper, never()).convertToTmsAttributeRS(any());
  }

  @Test
  void shouldGetEntityById() {
    // Given
    var attributeId = 1L;
    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(attributeEntity));

    // When
    var result = tmsAttributeService.getEntityById(PROJECT_ID, attributeId);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    assertThat(result).isEqualTo(attributeEntity);
  }

  @Test
  void shouldThrowExceptionWhenEntityNotFoundById() {
    // Given
    var attributeId = 1L;
    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.empty());

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.getEntityById(PROJECT_ID, attributeId))
        .isInstanceOf(ReportPortalException.class)
        .hasMessageContaining("TMS Attribute with id '" + attributeId + "' in project '" + PROJECT_ID + "'");

    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
  }

  @Test
  void shouldFindOrCreateTag_WhenTagExists() {
    // Given
    var key = "existing-tag";
    var existingTag = createAttributeEntityWithKeyAndValue(1L, key, null);

    when(tmsAttributeRepository.findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key))
        .thenReturn(Optional.of(existingTag));

    // When
    var result = tmsAttributeService.findOrCreateTag(PROJECT_ID, key);

    // Then
    verify(tmsAttributeRepository).findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key);
    verify(tmsAttributeRepository, never()).save(any());
    assertThat(result).isEqualTo(existingTag);
  }

  @Test
  void shouldFindOrCreateTag_WhenTagDoesNotExist() {
    // Given
    var key = "new-tag";
    var newTag = createAttributeEntityWithKeyAndValue(1L, key, null);

    when(tmsAttributeRepository.findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key))
        .thenReturn(Optional.empty());
    when(tmsAttributeMapper.convertToTmsAttribute(PROJECT_ID, key))
        .thenReturn(newTag);
    when(tmsAttributeRepository.save(newTag))
        .thenReturn(newTag);

    // When
    var result = tmsAttributeService.findOrCreateTag(PROJECT_ID, key);

    // Then
    verify(tmsAttributeRepository).findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key);
    verify(tmsAttributeMapper).convertToTmsAttribute(PROJECT_ID, key);
    verify(tmsAttributeRepository).save(newTag);
    assertThat(result).isEqualTo(newTag);
  }

  @Test
  void shouldFindOrCreateAttribute_WhenAttributeExists() {
    // Given
    var key = "existing-key";
    var value = "existing-value";
    var existingAttribute = createAttributeEntityWithKeyAndValue(1L, key, value);

    when(tmsAttributeRepository.findByProjectIdAndKeyAndValue(PROJECT_ID, key, value))
        .thenReturn(Optional.of(existingAttribute));

    // When
    var result = tmsAttributeService.findOrCreateAttribute(PROJECT_ID, key, value);

    // Then
    verify(tmsAttributeRepository).findByProjectIdAndKeyAndValue(PROJECT_ID, key, value);
    verify(tmsAttributeRepository, never()).save(any());
    assertThat(result).isEqualTo(existingAttribute);
  }

  @Test
  void shouldFindOrCreateAttribute_WhenAttributeDoesNotExist() {
    // Given
    var key = "new-key";
    var value = "new-value";
    var newAttribute = createAttributeEntityWithKeyAndValue(1L, key, value);

    when(tmsAttributeRepository.findByProjectIdAndKeyAndValue(PROJECT_ID, key, value))
        .thenReturn(Optional.empty());
    when(tmsAttributeMapper.convertToTmsAttribute(PROJECT_ID, key, value))
        .thenReturn(newAttribute);
    when(tmsAttributeRepository.save(newAttribute))
        .thenReturn(newAttribute);

    // When
    var result = tmsAttributeService.findOrCreateAttribute(PROJECT_ID, key, value);

    // Then
    verify(tmsAttributeRepository).findByProjectIdAndKeyAndValue(PROJECT_ID, key, value);
    verify(tmsAttributeMapper).convertToTmsAttribute(PROJECT_ID, key, value);
    verify(tmsAttributeRepository).save(newAttribute);
    assertThat(result).isEqualTo(newAttribute);
  }

  @Test
  void shouldGetKeysByCriteria() {
    // Given
    var search = "test";
    var keys = List.of("test-key1", "test-key2", "test-key3");

    when(tmsAttributeRepository.findDistinctKeysByProjectIdAndKeyLike(PROJECT_ID, search))
        .thenReturn(keys);

    // When
    var result = tmsAttributeService.getKeysByCriteria(PROJECT_ID, search);

    // Then
    verify(tmsAttributeRepository).findDistinctKeysByProjectIdAndKeyLike(PROJECT_ID, search);
    assertThat(result).hasSize(3);
    assertThat(result).containsExactlyElementsOf(keys);
  }

  @Test
  void shouldGetKeysByCriteriaWhenEmpty() {
    // Given
    var search = "nonexistent";

    when(tmsAttributeRepository.findDistinctKeysByProjectIdAndKeyLike(PROJECT_ID, search))
        .thenReturn(Collections.emptyList());

    // When
    var result = tmsAttributeService.getKeysByCriteria(PROJECT_ID, search);

    // Then
    verify(tmsAttributeRepository).findDistinctKeysByProjectIdAndKeyLike(PROJECT_ID, search);
    assertThat(result).isEmpty();
  }

  @Test
  void shouldGetKeysByCriteriaWithNullSearch() {
    // Given
    var keys = List.of("key1", "key2");

    when(tmsAttributeRepository.findDistinctKeysByProjectIdAndKeyLike(PROJECT_ID, null))
        .thenReturn(keys);

    // When
    var result = tmsAttributeService.getKeysByCriteria(PROJECT_ID, null);

    // Then
    verify(tmsAttributeRepository).findDistinctKeysByProjectIdAndKeyLike(PROJECT_ID, null);
    assertThat(result).hasSize(2);
  }

  @Test
  void shouldGetValuesByCriteria() {
    // Given
    var search = "prod";
    var values = List.of("production", "product");

    when(tmsAttributeRepository.findDistinctValuesByProjectIdAndValueLike(PROJECT_ID, search))
        .thenReturn(values);

    // When
    var result = tmsAttributeService.getValuesByCriteria(PROJECT_ID, search);

    // Then
    verify(tmsAttributeRepository).findDistinctValuesByProjectIdAndValueLike(PROJECT_ID, search);
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(values);
  }

  @Test
  void shouldGetValuesByCriteriaWhenEmpty() {
    // Given
    var search = "nonexistent";

    when(tmsAttributeRepository.findDistinctValuesByProjectIdAndValueLike(PROJECT_ID, search))
        .thenReturn(Collections.emptyList());

    // When
    var result = tmsAttributeService.getValuesByCriteria(PROJECT_ID, search);

    // Then
    verify(tmsAttributeRepository).findDistinctValuesByProjectIdAndValueLike(PROJECT_ID, search);
    assertThat(result).isEmpty();
  }

  @Test
  void shouldGetValuesByCriteriaWithNullSearch() {
    // Given
    var values = List.of("value1", "value2");

    when(tmsAttributeRepository.findDistinctValuesByProjectIdAndValueLike(PROJECT_ID, null))
        .thenReturn(values);

    // When
    var result = tmsAttributeService.getValuesByCriteria(PROJECT_ID, null);

    // Then
    verify(tmsAttributeRepository).findDistinctValuesByProjectIdAndValueLike(PROJECT_ID, null);
    assertThat(result).hasSize(2);
  }

  @Test
  void shouldResolveAttributesWithEmptySet() {
    // Given
    Set<String> emptyKeys = Collections.emptySet();

    // When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, emptyKeys);

    // Then
    assertThat(result).isEmpty();
    verify(tmsAttributeRepository, never()).findAllByProject_IdAndKeyIn(any(), any());
  }

  @Test
  void shouldResolveAttributesWithNullSet() {
    // Given & When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, null);

    // Then
    assertThat(result).isEmpty();
    verify(tmsAttributeRepository, never()).findAllByProject_IdAndKeyIn(any(), any());
  }

  @Test
  void shouldResolveAttributesWithExistingAttributes() {
    // Given
    var keys = Set.of("key1", "key2");

    var attr1 = createAttributeEntityWithKeyAndValue(1L, "key1", "value1");
    var attr2 = createAttributeEntityWithKeyAndValue(2L, "key2", "value2");

    when(tmsAttributeRepository.findAllByProject_IdAndKeyIn(PROJECT_ID, keys))
        .thenReturn(List.of(attr1, attr2));

    // When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, keys);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get("key1")).isEqualTo(1L);
    assertThat(result.get("key2")).isEqualTo(2L);

    verify(tmsAttributeRepository).findAllByProject_IdAndKeyIn(PROJECT_ID, keys);
    verify(tmsAttributeRepository, never()).save(any());
  }

  @Test
  void shouldResolveAttributesCreatingMissingOnes() {
    // Given
    var keys = Set.of("existing", "new");

    var existingAttr = createAttributeEntityWithKeyAndValue(1L, "existing", "value");
    var newAttr = createAttributeEntityWithKeyAndValue(2L, "new", null);

    when(tmsAttributeRepository.findAllByProject_IdAndKeyIn(PROJECT_ID, keys))
        .thenReturn(List.of(existingAttr));
    when(tmsAttributeMapper.convertToTmsAttribute(PROJECT_ID, "new"))
        .thenReturn(newAttr);
    when(tmsAttributeRepository.save(newAttr))
        .thenReturn(newAttr);

    // When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, keys);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get("existing")).isEqualTo(1L);
    assertThat(result.get("new")).isEqualTo(2L);

    verify(tmsAttributeRepository).findAllByProject_IdAndKeyIn(PROJECT_ID, keys);
    verify(tmsAttributeMapper).convertToTmsAttribute(PROJECT_ID, "new");
    verify(tmsAttributeRepository).save(newAttr);
  }

  @Test
  void shouldResolveAttributesCreatingAllWhenNoneExist() {
    // Given
    var keys = Set.of("new1", "new2");

    var newAttr1 = createAttributeEntityWithKeyAndValue(1L, "new1", null);
    var newAttr2 = createAttributeEntityWithKeyAndValue(2L, "new2", null);

    when(tmsAttributeRepository.findAllByProject_IdAndKeyIn(PROJECT_ID, keys))
        .thenReturn(List.of());
    when(tmsAttributeMapper.convertToTmsAttribute(eq(PROJECT_ID), anyString()))
        .thenAnswer(invocation -> {
          var key = (String) invocation.getArgument(1);
          return "new1".equals(key) ? newAttr1 : newAttr2;
        });
    when(tmsAttributeRepository.save(any(TmsAttribute.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, keys);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsEntry("new1", 1L);
    assertThat(result).containsEntry("new2", 2L);

    verify(tmsAttributeRepository).findAllByProject_IdAndKeyIn(PROJECT_ID, keys);
    verify(tmsAttributeRepository, times(2)).save(any());
  }

  @Test
  void shouldResolveAttributesFilteringNullAndBlankKeys() {
    // Given
    Set<String> keysWithNulls = new HashSet<>();
    keysWithNulls.add("valid");
    keysWithNulls.add(null);
    keysWithNulls.add("");
    keysWithNulls.add("   ");

    var validAttr = createAttributeEntityWithKeyAndValue(1L, "valid", "value");

    when(tmsAttributeRepository.findAllByProject_IdAndKeyIn(eq(PROJECT_ID), anySet()))
        .thenReturn(List.of(validAttr));

    // When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, keysWithNulls);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get("valid")).isEqualTo(1L);

    // Verify that only valid key was passed to repository
    ArgumentCaptor<Set<String>> keysCaptor = ArgumentCaptor.forClass(Set.class);
    verify(tmsAttributeRepository).findAllByProject_IdAndKeyIn(eq(PROJECT_ID), keysCaptor.capture());
    assertThat(keysCaptor.getValue()).containsExactly("valid");
  }

  @Test
  void shouldResolveAttributesReturningEmptyMapWhenAllKeysAreInvalid() {
    // Given
    Set<String> invalidKeys = new HashSet<>();
    invalidKeys.add(null);
    invalidKeys.add("");
    invalidKeys.add("   ");

    // When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, invalidKeys);

    // Then
    assertThat(result).isEmpty();
    verify(tmsAttributeRepository, never()).findAllByProject_IdAndKeyIn(any(), any());
  }

  @Test
  void shouldResolveAttributesWithSingleKey() {
    // Given
    var keys = Set.of("singleKey");

    var attr = createAttributeEntityWithKeyAndValue(1L, "singleKey", "value");

    when(tmsAttributeRepository.findAllByProject_IdAndKeyIn(PROJECT_ID, keys))
        .thenReturn(List.of(attr));

    // When
    var result = tmsAttributeService.resolveAttributes(PROJECT_ID, keys);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get("singleKey")).isEqualTo(1L);

    verify(tmsAttributeRepository).findAllByProject_IdAndKeyIn(PROJECT_ID, keys);
  }

  @Test
  void shouldCreateAttributeWithNullValue() {
    // Given
    var requestWithNullValue = TmsAttributeRQ.builder()
        .key("test-key")
        .value(null)
        .build();
    var entityWithNullValue = createAttributeEntityWithKeyAndValue(1L, "test-key", null);
    var responseWithNullValue = createAttributeResponseWithKeyAndValue("test-key", null);

    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID, "test-key", null))
        .thenReturn(false);
    when(tmsAttributeMapper.convertToTmsAttribute(requestWithNullValue, PROJECT_ID))
        .thenReturn(entityWithNullValue);
    when(tmsAttributeRepository.save(entityWithNullValue))
        .thenReturn(entityWithNullValue);
    when(tmsAttributeMapper.convertToTmsAttributeRS(entityWithNullValue))
        .thenReturn(responseWithNullValue);

    // When
    var result = tmsAttributeService.create(PROJECT_ID, requestWithNullValue);

    // Then
    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID, "test-key", null);
    verify(tmsAttributeRepository).save(entityWithNullValue);
    assertThat(result).isEqualTo(responseWithNullValue);
  }

  @Test
  void shouldPatchAttributeWithNullKeyInRequest() {
    // Given
    var attributeId = 1L;
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .value("new-value")
        .build();
    var existingEntity = createAttributeEntity();
    var updatedEntity = createAttributeEntity();
    var updatedResponse = createAttributeResponse();

    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID))
        .thenReturn(Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByProjectIdAndKeyAndValue(PROJECT_ID, "test-key", "new-value"))
        .thenReturn(false);
    when(tmsAttributeRepository.save(existingEntity))
        .thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity))
        .thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(PROJECT_ID, attributeId, requestWithNullKey);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeRepository).existsByProjectIdAndKeyAndValue(PROJECT_ID, "test-key", "new-value");
    verify(tmsAttributeMapper).patch(existingEntity, requestWithNullKey);
    assertThat(result).isEqualTo(updatedResponse);
  }

  @Test
  void shouldFindOrCreateMultipleTags() {
    // Given
    var key1 = "tag1";
    var key2 = "tag2";
    var existingTag = createAttributeEntityWithKeyAndValue(1L, key1, null);
    var newTag = createAttributeEntityWithKeyAndValue(2L, key2, null);

    when(tmsAttributeRepository.findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key1))
        .thenReturn(Optional.of(existingTag));
    when(tmsAttributeRepository.findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key2))
        .thenReturn(Optional.empty());
    when(tmsAttributeMapper.convertToTmsAttribute(PROJECT_ID, key2))
        .thenReturn(newTag);
    when(tmsAttributeRepository.save(newTag))
        .thenReturn(newTag);

    // When
    var result1 = tmsAttributeService.findOrCreateTag(PROJECT_ID, key1);
    var result2 = tmsAttributeService.findOrCreateTag(PROJECT_ID, key2);

    // Then
    assertThat(result1).isEqualTo(existingTag);
    assertThat(result2).isEqualTo(newTag);
    verify(tmsAttributeRepository).findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key1);
    verify(tmsAttributeRepository).findByProject_IdAndKeyAndValueIsNull(PROJECT_ID, key2);
    verify(tmsAttributeRepository, times(1)).save(newTag);
  }

  @Test
  void shouldFindOrCreateMultipleAttributes() {
    // Given
    var key1 = "key1";
    var value1 = "value1";
    var key2 = "key2";
    var value2 = "value2";
    var existingAttr = createAttributeEntityWithKeyAndValue(1L, key1, value1);
    var newAttr = createAttributeEntityWithKeyAndValue(2L, key2, value2);

    when(tmsAttributeRepository.findByProjectIdAndKeyAndValue(PROJECT_ID, key1, value1))
        .thenReturn(Optional.of(existingAttr));
    when(tmsAttributeRepository.findByProjectIdAndKeyAndValue(PROJECT_ID, key2, value2))
        .thenReturn(Optional.empty());
    when(tmsAttributeMapper.convertToTmsAttribute(PROJECT_ID, key2, value2))
        .thenReturn(newAttr);
    when(tmsAttributeRepository.save(newAttr))
        .thenReturn(newAttr);

    // When
    var result1 = tmsAttributeService.findOrCreateAttribute(PROJECT_ID, key1, value1);
    var result2 = tmsAttributeService.findOrCreateAttribute(PROJECT_ID, key2, value2);

    // Then
    assertThat(result1).isEqualTo(existingAttr);
    assertThat(result2).isEqualTo(newAttr);
    verify(tmsAttributeRepository).findByProjectIdAndKeyAndValue(PROJECT_ID, key1, value1);
    verify(tmsAttributeRepository).findByProjectIdAndKeyAndValue(PROJECT_ID, key2, value2);
    verify(tmsAttributeRepository, times(1)).save(newAttr);
  }

  // Helper method for resolve attributes and other tests
  private TmsAttribute createAttributeEntityWithKeyAndValue(Long id, String key, String value) {
    var entity = new TmsAttribute();
    entity.setId(id);
    entity.setKey(key);
    entity.setValue(value);
    var project = new Project();
    project.setId(PROJECT_ID);
    entity.setProject(project);
    return entity;
  }

  // Helper methods
  private TmsAttributeRQ createAttributeRequest() {
    return TmsAttributeRQ.builder()
        .key("test-key")
        .value("test-value")
        .build();
  }

  private TmsAttributeRQ createUpdatedAttributeRequest() {
    return TmsAttributeRQ.builder()
        .key("updated-key")
        .value("updated-value")
        .build();
  }

  private TmsAttribute createAttributeEntity() {
    return createAttributeEntityWithKeyAndValue(1L, "test-key", "test-value");
  }

  private TmsAttribute createUpdatedAttributeEntity() {
    return createAttributeEntityWithKeyAndValue(1L, "updated-key", "updated-value");
  }

  private TmsAttributeRS createAttributeResponse() {
    return createAttributeResponseWithKeyAndValue("test-key", "test-value");
  }

  private TmsAttributeRS createUpdatedAttributeResponse() {
    return createAttributeResponseWithKeyAndValue("updated-key", "updated-value");
  }

  private TmsAttributeRS createAttributeResponseWithKeyAndValue(String key, String value) {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey(key);
    response.setValue(value);
    return response;
  }
}
