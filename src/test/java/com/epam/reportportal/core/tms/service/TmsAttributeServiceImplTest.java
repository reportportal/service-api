package com.epam.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import jakarta.persistence.EntityExistsException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    when(tmsAttributeRepository.existsByKeyAndProject_Id(attributeRequest.getKey(),
        PROJECT_ID)).thenReturn(false);
    when(tmsAttributeMapper.convertToTmsAttribute(attributeRequest, PROJECT_ID)).thenReturn(
        attributeEntity);
    when(tmsAttributeRepository.save(attributeEntity)).thenReturn(attributeEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attributeEntity)).thenReturn(attributeResponse);

    // When
    var result = tmsAttributeService.create(PROJECT_ID, attributeRequest);

    // Then
    verify(tmsAttributeRepository).existsByKeyAndProject_Id(attributeRequest.getKey(), PROJECT_ID);
    verify(tmsAttributeMapper).convertToTmsAttribute(attributeRequest, PROJECT_ID);
    verify(tmsAttributeRepository).save(any(TmsAttribute.class));
    verify(tmsAttributeMapper).convertToTmsAttributeRS(attributeEntity);

    assertThat(result).isEqualTo(attributeResponse);
  }

  @Test
  void shouldThrowEntityExistsExceptionWhenKeyAlreadyExistsInProject() {
    // Given
    when(tmsAttributeRepository.existsByKeyAndProject_Id(attributeRequest.getKey(),
        PROJECT_ID)).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> tmsAttributeService.create(PROJECT_ID, attributeRequest))
        .isInstanceOf(EntityExistsException.class)
        .hasMessageContaining("TMS Attribute with key '" + attributeRequest.getKey()
            + "' already exists in project '" + PROJECT_ID + "'");

    verify(tmsAttributeRepository).existsByKeyAndProject_Id(attributeRequest.getKey(), PROJECT_ID);
    verify(tmsAttributeMapper, never()).convertToTmsAttribute(any(), eq(PROJECT_ID));
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

    when(tmsAttributeRepository.findByIdAndProject_Id(attributeId, PROJECT_ID)).thenReturn(
        Optional.of(existingEntity));
    when(tmsAttributeRepository.existsByKeyAndProject_Id(updatedRequest.getKey(),
        PROJECT_ID)).thenReturn(false);
    when(tmsAttributeRepository.save(existingEntity)).thenReturn(updatedEntity);
    when(tmsAttributeMapper.convertToTmsAttributeRS(updatedEntity)).thenReturn(updatedResponse);

    // When
    var result = tmsAttributeService.patch(PROJECT_ID, attributeId, updatedRequest);

    // Then
    verify(tmsAttributeRepository).findByIdAndProject_Id(attributeId, PROJECT_ID);
    verify(tmsAttributeRepository).existsByKeyAndProject_Id(updatedRequest.getKey(), PROJECT_ID);
    verify(tmsAttributeMapper).patch(existingEntity, updatedRequest);
    verify(tmsAttributeRepository).save(existingEntity);
    verify(tmsAttributeMapper).convertToTmsAttributeRS(updatedEntity);

    assertThat(result).isEqualTo(updatedResponse);
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
    var project = new Project();
    project.setId(PROJECT_ID);
    entity.setProject(project);
    return entity;
  }

  private TmsAttribute createUpdatedAttributeEntity() {
    var entity = new TmsAttribute();
    entity.setId(1L);
    entity.setKey("updated-key");
    var project = new Project();
    project.setId(PROJECT_ID);
    entity.setProject(project);
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
