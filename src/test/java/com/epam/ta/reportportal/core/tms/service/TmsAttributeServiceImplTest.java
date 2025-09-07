package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.db.repository.TmsAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsAttributeMapper;
import jakarta.persistence.EntityExistsException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TmsAttributeServiceImplTest {

  @Mock
  private TmsAttributeRepository tmsAttributeRepository;

  @Mock
  private TmsAttributeMapper tmsAttributeMapper;

  @InjectMocks
  private TmsAttributeServiceImpl tmsAttributeService;

  private TmsAttributeRQ attributeRequest;
  private TmsAttribute attributeEntity;
  private TmsAttributeRS attributeResponse;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    attributeRequest = createAttributeRequest();
    attributeEntity = createAttributeEntity();
    attributeResponse = createAttributeResponse();
    pageable = PageRequest.of(0, 10);
  }

  @Test
  void shouldCreateAttribute() {
    // Given
    when(tmsAttributeRepository.existsByKey(attributeRequest.getKey())).thenReturn(false);
    when(tmsAttributeMapper.convertToTmsAttribute(attributeRequest)).thenReturn(attributeEntity);
    when(tmsAttributeRepository.save(attributeEntity)).thenReturn(attributeEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attributeEntity)).thenReturn(attributeResponse);

    // When
    var result = tmsAttributeService.create(attributeRequest);

    // Then
    verify(tmsAttributeRepository).existsByKey(attributeRequest.getKey());
    verify(tmsAttributeMapper).convertToTmsAttribute(attributeRequest);
    verify(tmsAttributeRepository).save(attributeEntity);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(attributeEntity);

    assertThat(result).isEqualTo(attributeResponse);
  }

  @Test
  void shouldThrowEntityExistsExceptionWhenKeyAlreadyExists() {
    // Given
    when(tmsAttributeRepository.existsByKey(attributeRequest.getKey())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.create(attributeRequest))
        .isInstanceOf(EntityExistsException.class)
        .hasMessageContaining("TMS Attribute with key '" + attributeRequest.getKey() + "' already exists");

    verify(tmsAttributeRepository).existsByKey(attributeRequest.getKey());
    verify(tmsAttributeMapper, never()).convertToTmsAttribute(any());
    verify(tmsAttributeRepository, never()).save(any());
    verify(tmsAttributeMapper, never()).convertToTmsAttributeRS(any());
  }

  @Test
  void shouldPatchAttribute() {
    // Given
    var attributeId = 1L;
    var updatedRequest = createUpdatedAttributeRequest();
    var existingEntity = createAttributeEntity();
    var updatedEntity = createUpdatedAttributeEntity();
    var updatedResponse = createUpdatedAttributeResponse();

    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByKey(updatedRequest.getKey())).thenReturn(false);
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity)).thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(attributeId, updatedRequest);

    // Then
    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeRepository).existsByKey(updatedRequest.getKey());
    verify(tmsAttributeMapper).patch(existingEntity, updatedRequest);
    verify(tmsAttributeRepository).save(existingEntity);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(updatedEntity);

    assertThat(result).isEqualTo(updatedResponse);
  }

  @Test
  void shouldPatchAttributeWithSameKey() {
    // Given
    var attributeId = 1L;
    var existingEntity = createAttributeEntity();
    var requestWithSameKey = TmsAttributeRQ.builder()
        .key(existingEntity.getKey())
        .build();

    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.of(existingEntity));
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(existingEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(existingEntity)).thenReturn(attributeResponse);

    // When
    var result = tmsAttributeService.patch(attributeId, requestWithSameKey);

    // Then
    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeRepository, never()).existsByKey(any());
    verify(tmsAttributeMapper).patch(existingEntity, requestWithSameKey);
    verify(tmsAttributeRepository).save(existingEntity);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(existingEntity);

    assertThat(result).isEqualTo(attributeResponse);
  }

  @Test
  void shouldNotValidateKeyUniquenessWhenKeyIsNull() {
    // Given
    var attributeId = 1L;
    var existingEntity = createAttributeEntity();
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .build();

    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.of(existingEntity));
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(existingEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(existingEntity)).thenReturn(attributeResponse);

    // When
    var result = tmsAttributeService.patch(attributeId, requestWithNullKey);

    // Then
    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeRepository, never()).existsByKey(any());
    verify(tmsAttributeMapper).patch(existingEntity, requestWithNullKey);
    verify(tmsAttributeRepository).save(existingEntity);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(existingEntity);

    assertThat(result).isEqualTo(attributeResponse);
  }

  @Test
  void shouldThrowEntityExistsExceptionWhenPatchingWithExistingKey() {
    // Given
    var attributeId = 1L;
    var existingEntity = createAttributeEntity();
    var updatedRequest = createUpdatedAttributeRequest();

    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByKey(updatedRequest.getKey())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.patch(attributeId, updatedRequest))
        .isInstanceOf(EntityExistsException.class)
        .hasMessageContaining("TMS Attribute with key '" + updatedRequest.getKey() + "' already exists");

    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeRepository).existsByKey(updatedRequest.getKey());
    verify(tmsAttributeMapper, never()).patch(any(), any());
    verify(tmsAttributeRepository, never()).save(any());
    verify(tmsAttributeMapper, never()).convertToTmsAttributeRS(any());
  }

  @Test
  void shouldThrowReportPortalExceptionWhenAttributeNotFoundForPatch() {
    // Given
    var attributeId = 1L;
    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.patch(attributeId, attributeRequest))
        .isInstanceOf(ReportPortalException.class)
        .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND)
        .hasMessageContaining("TMS Attribute with id '" + attributeId);

    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeRepository, never()).existsByKey(any());
    verify(tmsAttributeMapper, never()).patch(any(), any());
    verify(tmsAttributeRepository, never()).save(any());
    verify(tmsAttributeMapper, never()).convertToTmsAttributeRS(any());
  }

  @Test
  void shouldCallRepositoryFindAllForGetAllAttributes() {
    // Given
    var attributes = List.of(attributeEntity, createUpdatedAttributeEntity());
    var page = new PageImpl<>(attributes, pageable, attributes.size());

    when(tmsAttributeRepository.findAll(pageable)).thenReturn(page);

    // When
    tmsAttributeService.getAll(pageable);

    // Then
    verify(tmsAttributeRepository).findAll(pageable);
  }

  @Test
  void shouldReturnPageWhenAttributesExist() {
    // Given
    var attributes = List.of(attributeEntity);
    var page = new PageImpl<>(attributes, pageable, attributes.size());

    when(tmsAttributeRepository.findAll(pageable)).thenReturn(page);

    // When
    var result = tmsAttributeService.getAll(pageable);

    // Then
    verify(tmsAttributeRepository).findAll(pageable);
    assertThat(result).isNotNull();
  }

  @Test
  void shouldReturnEmptyPageWhenNoAttributes() {
    // Given
    var emptyPage = new PageImpl<TmsAttribute>(Collections.emptyList(), pageable, 0);

    when(tmsAttributeRepository.findAll(pageable)).thenReturn(emptyPage);

    // When
    var result = tmsAttributeService.getAll(pageable);

    // Then
    verify(tmsAttributeRepository).findAll(pageable);
    assertThat(result).isNotNull();
  }

  @Test
  void shouldGetAttributeById() {
    // Given
    var attributeId = 1L;
    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.of(attributeEntity));
    when(tmsAttributeMapper.convertToTmsAttributeRS(attributeEntity)).thenReturn(attributeResponse);

    // When
    var result = tmsAttributeService.getById(attributeId);

    // Then
    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(attributeEntity);

    assertThat(result).isEqualTo(attributeResponse);
  }

  @Test
  void shouldThrowReportPortalExceptionWhenAttributeNotFoundById() {
    // Given
    var attributeId = 1L;
    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.getById(attributeId))
        .isInstanceOf(ReportPortalException.class)
        .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND)
        .hasMessageContaining("TMS Attribute with id '" + attributeId);

    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeMapper, never()).convertToTmsAttributeRS(any());
  }

  @Test
  void shouldCreateAttributeWithNullKey() {
    // Given
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .build();

    var entityWithNullKey = new TmsAttribute();
    entityWithNullKey.setId(1L);
    entityWithNullKey.setKey(null);

    var responseWithNullKey = new TmsAttributeRS();
    responseWithNullKey.setId(1L);
    responseWithNullKey.setKey(null);

    when(tmsAttributeRepository.existsByKey(null)).thenReturn(false);
    when(tmsAttributeMapper.convertToTmsAttribute(requestWithNullKey)).thenReturn(entityWithNullKey);
    when(tmsAttributeRepository.save(entityWithNullKey)).thenReturn(entityWithNullKey);
    when(tmsAttributeMapper.convertToTmsAttributeRS(entityWithNullKey)).thenReturn(responseWithNullKey);

    // When
    var result = tmsAttributeService.create(requestWithNullKey);

    // Then
    verify(tmsAttributeRepository).existsByKey(null);
    verify(tmsAttributeMapper).convertToTmsAttribute(requestWithNullKey);
    verify(tmsAttributeRepository).save(entityWithNullKey);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(entityWithNullKey);

    assertThat(result).isEqualTo(responseWithNullKey);
  }

  @Test
  void shouldCreateAttributeWithEmptyKey() {
    // Given
    var requestWithEmptyKey = TmsAttributeRQ.builder()
        .key("")
        .build();

    var entityWithEmptyKey = new TmsAttribute();
    entityWithEmptyKey.setId(1L);
    entityWithEmptyKey.setKey("");

    var responseWithEmptyKey = new TmsAttributeRS();
    responseWithEmptyKey.setId(1L);
    responseWithEmptyKey.setKey("");

    when(tmsAttributeRepository.existsByKey("")).thenReturn(false);
    when(tmsAttributeMapper.convertToTmsAttribute(requestWithEmptyKey)).thenReturn(entityWithEmptyKey);
    when(tmsAttributeRepository.save(entityWithEmptyKey)).thenReturn(entityWithEmptyKey);
    when(tmsAttributeMapper.convertToTmsAttributeRS(entityWithEmptyKey)).thenReturn(responseWithEmptyKey);

    // When
    var result = tmsAttributeService.create(requestWithEmptyKey);

    // Then
    verify(tmsAttributeRepository).existsByKey("");
    verify(tmsAttributeMapper).convertToTmsAttribute(requestWithEmptyKey);
    verify(tmsAttributeRepository).save(entityWithEmptyKey);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(entityWithEmptyKey);

    assertThat(result).isEqualTo(responseWithEmptyKey);
  }

  @Test
  void shouldPatchAttributeToNullKey() {
    // Given
    var attributeId = 1L;
    var existingEntity = createAttributeEntity();
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .build();

    var updatedEntity = new TmsAttribute();
    updatedEntity.setId(1L);
    updatedEntity.setKey(null);

    var updatedResponse = new TmsAttributeRS();
    updatedResponse.setId(1L);
    updatedResponse.setKey(null);

    when(tmsAttributeRepository.findById(attributeId)).thenReturn(Optional.of(existingEntity));
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity)).thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(attributeId, requestWithNullKey);

    // Then
    verify(tmsAttributeRepository).findById(attributeId);
    verify(tmsAttributeRepository, never()).existsByKey(any());
    verify(tmsAttributeMapper).patch(existingEntity, requestWithNullKey);
    verify(tmsAttributeRepository).save(existingEntity);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(updatedEntity);

    assertThat(result).isEqualTo(updatedResponse);
  }

  // Helper methods
  private TmsAttributeRQ createAttributeRequest() {
    return TmsAttributeRQ.builder()
        .key("test-key")
        .build();
  }

  private TmsAttributeRQ createUpdatedAttributeRequest() {
    return TmsAttributeRQ.builder()
        .key("updated-key")
        .build();
  }

  private TmsAttribute createAttributeEntity() {
    var entity = new TmsAttribute();
    entity.setId(1L);
    entity.setKey("test-key");
    return entity;
  }

  private TmsAttribute createUpdatedAttributeEntity() {
    var entity = new TmsAttribute();
    entity.setId(1L);
    entity.setKey("updated-key");
    return entity;
  }

  private TmsAttributeRS createAttributeResponse() {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey("test-key");
    return response;
  }

  private TmsAttributeRS createUpdatedAttributeResponse() {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey("updated-key");
    return response;
  }
}
