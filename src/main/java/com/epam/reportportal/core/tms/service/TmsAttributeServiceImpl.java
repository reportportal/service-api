package com.epam.reportportal.core.tms.service;

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
import com.epam.reportportal.ws.converter.PagedResourcesAssembler;
import jakarta.persistence.EntityExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmsAttributeServiceImpl implements TmsAttributeService {

  private final TmsAttributeFilterableRepository tmsAttributeFilterableRepository;
  private final TmsAttributeRepository tmsAttributeRepository;
  private final TmsAttributeMapper tmsAttributeMapper;

  @Override
  @Transactional
  public TmsAttributeRS create(TmsAttributeRQ request) {
    validateKeyUniqueness(request.getKey());

    var entity = tmsAttributeMapper.convertToTmsAttribute(request);
    var savedEntity = tmsAttributeRepository.save(entity);

    return tmsAttributeMapper.convertToTmsAttributeRS(savedEntity);
  }

  @Override
  @Transactional
  public TmsAttributeRS patch(Long attributeId, TmsAttributeRQ request) {
    var existingAttribute = findAttributeById(attributeId);

    if (Objects.nonNull(request.getKey())
        && !request.getKey().equals(existingAttribute.getKey())) {
      validateKeyUniqueness(request.getKey());
    }

    tmsAttributeMapper.patch(existingAttribute, request);

    return tmsAttributeMapper.convertToTmsAttributeRS(
        tmsAttributeRepository.save(existingAttribute)
    );
  }

  @Override
  @Transactional(readOnly = true)
  public com.epam.reportportal.model.Page<TmsAttributeRS> getAll(Filter filter, Pageable pageable) {
    return PagedResourcesAssembler
        .pageConverter(tmsAttributeMapper::convertToTmsAttributeRS)
        .apply(tmsAttributeFilterableRepository.findByFilter(filter, pageable));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsAttributeRS getById(Long attributeId) {
    return tmsAttributeMapper.convertToTmsAttributeRS(
        findAttributeById(attributeId)
    );
  }

  @Override
  @Transactional
  public Map<String, Long> resolveAttributes(Long projectId, List<String> attributeKeys) {
    if (CollectionUtils.isEmpty(attributeKeys)) {
      return Map.of();
    }

    Set<String> uniqueKeys = attributeKeys.stream()
        .filter(key -> key != null && !key.isBlank())
        .collect(Collectors.toSet());

    return resolveAttributesBatch(projectId, uniqueKeys);
  }

  @Override
  @Transactional
  public Map<String, Long> resolveAttributesBatch(Long projectId, Set<String> allAttributeKeys) {
    if (CollectionUtils.isEmpty(allAttributeKeys)) {
      return Map.of();
    }

    Set<String> uniqueKeys = allAttributeKeys.stream()
        .filter(key -> key != null && !key.isBlank())
        .collect(Collectors.toSet());

    if (uniqueKeys.isEmpty()) {
      return Map.of();
    }

    Map<String, Long> result = new HashMap<>();

    // Find existing attributes
    List<TmsAttribute> existingAttributes = tmsAttributeRepository
        .findAllByProject_IdAndKeyIn(projectId, uniqueKeys);

    existingAttributes.forEach(attr -> result.put(attr.getKey(), attr.getId()));

    // Create missing attributes
    Set<String> existingKeys = existingAttributes.stream()
        .map(TmsAttribute::getKey)
        .collect(Collectors.toSet());

    Set<String> missingKeys = uniqueKeys.stream()
        .filter(key -> !existingKeys.contains(key))
        .collect(Collectors.toSet());

    for (String key : missingKeys) {
      Long attributeId = createAttributeInternal(projectId, key);
      result.put(key, attributeId);
    }

    return result;
  }

  private Long createAttributeInternal(Long projectId, String key) {
    TmsAttribute attribute = new TmsAttribute();
    attribute.setKey(key);

    Project project = new Project();
    project.setId(projectId);
    attribute.setProject(project);

    TmsAttribute savedAttribute = tmsAttributeRepository.save(attribute);
    log.debug("Created attribute '{}' with ID {} for project {}", key, savedAttribute.getId(),
        projectId);

    return savedAttribute.getId();
  }

  private TmsAttribute findAttributeById(Long attributeId) {
    return tmsAttributeRepository
        .findById(attributeId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "TMS Attribute with id '" + attributeId + "' not found"));
  }

  private void validateKeyUniqueness(String key) {
    if (tmsAttributeRepository.existsByKey(key)) {
      throw new EntityExistsException(
          "TMS Attribute with key '" + key + "' already exists");
    }
  }
}