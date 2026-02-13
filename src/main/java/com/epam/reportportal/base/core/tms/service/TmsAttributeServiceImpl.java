package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.base.core.tms.mapper.TmsAttributeMapper;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsAttributeFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import jakarta.persistence.EntityExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsAttributeServiceImpl implements TmsAttributeService {

  private final TmsAttributeFilterableRepository tmsAttributeFilterableRepository;
  private final TmsAttributeRepository tmsAttributeRepository;
  private final TmsAttributeMapper tmsAttributeMapper;

  @Override
  @Transactional
  public TmsAttributeRS create(Long projectId, TmsAttributeRQ request) {
    validateUniqueness(projectId, request.getKey(), request.getValue());

    var entity = tmsAttributeMapper.convertToTmsAttribute(request, projectId);

    var savedEntity = tmsAttributeRepository.save(entity);

    return tmsAttributeMapper.convertToTmsAttributeRS(savedEntity);
  }

  @Override
  @Transactional
  public TmsAttributeRS patch(Long projectId, Long attributeId, TmsAttributeRQ request) {
    var existingAttribute = findAttributeByIdAndProjectId(projectId, attributeId);

    var keyChanged = Objects.nonNull(request.getKey())
        && !request.getKey().equals(existingAttribute.getKey());
    var valueChanged = Objects.nonNull(request.getValue())
        && !request.getValue().equals(existingAttribute.getValue());

    if (keyChanged || valueChanged) {
      var newKey = keyChanged ? request.getKey() : existingAttribute.getKey();
      var newValue = valueChanged ? request.getValue() : existingAttribute.getValue();
      validateUniqueness(projectId, newKey, newValue);
    }

    tmsAttributeMapper.patch(existingAttribute, request);

    return tmsAttributeMapper.convertToTmsAttributeRS(
        tmsAttributeRepository.save(existingAttribute)
    );
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsAttributeRS> getAll(Long projectId, Filter filter,
      Pageable pageable) {
    return PagedResourcesAssembler
        .pageConverter(tmsAttributeMapper::convertToTmsAttributeRS)
        .apply(
            tmsAttributeFilterableRepository.findByProjectIdAndFilter(projectId, filter, pageable));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsAttributeRS getById(Long projectId, Long attributeId) {
    return tmsAttributeMapper.convertToTmsAttributeRS(
        findAttributeByIdAndProjectId(projectId, attributeId)
    );
  }

  @Override
  @Transactional(readOnly = true)
  public TmsAttribute getEntityById(Long projectId, Long attributeId) {
    return findAttributeByIdAndProjectId(projectId, attributeId);
  }

  @Override
  @Transactional
  public TmsAttribute findOrCreateTag(Long projectId, String key) {
    return tmsAttributeRepository
        .findByProject_IdAndKeyAndValueIsNull(projectId, key)
        .orElseGet(() -> tmsAttributeRepository.save(
            tmsAttributeMapper.convertToTmsAttribute(projectId, key)
        ));
  }

  @Override
  @Transactional
  public TmsAttribute findOrCreateAttribute(Long projectId, String key, String value) {
    return tmsAttributeRepository.findByProjectIdAndKeyAndValue(projectId, key, value)
        .orElseGet(() -> tmsAttributeRepository.save(
            tmsAttributeMapper.convertToTmsAttribute(projectId, key, value)
        ));
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> getKeysByCriteria(Long projectId, String search) {
    return tmsAttributeRepository.findDistinctKeysByProjectIdAndKeyLike(projectId, search);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> getValuesByCriteria(Long projectId, String search) {
    return tmsAttributeRepository.findDistinctValuesByProjectIdAndValueLike(projectId, search);
  }

  @Override
  @Transactional
  public Map<String, Long> resolveAttributes(Long projectId, Set<String> allAttributeKeys) {
    if (CollectionUtils.isEmpty(allAttributeKeys)) {
      return Map.of();
    }

    var uniqueKeys = allAttributeKeys.stream()
        .filter(key -> key != null && !key.isBlank())
        .collect(Collectors.toSet());

    if (uniqueKeys.isEmpty()) {
      return Map.of();
    }

    Map<String, Long> result = new HashMap<>();

    // Find existing attributes
    var existingAttributes = tmsAttributeRepository
        .findAllByProject_IdAndKeyIn(projectId, uniqueKeys);

    existingAttributes.forEach(attr -> result.put(attr.getKey(), attr.getId()));

    // Create missing attributes
    var existingKeys = existingAttributes.stream()
        .map(TmsAttribute::getKey)
        .collect(Collectors.toSet());

    var missingKeys = uniqueKeys.stream()
        .filter(key -> !existingKeys.contains(key))
        .collect(Collectors.toSet());

    for (String key : missingKeys) {
      var attributeId = createAttributeInternal(projectId, key);
      result.put(key, attributeId);
    }

    return result;
  }

  private Long createAttributeInternal(Long projectId, String key) {
    TmsAttribute attribute = tmsAttributeMapper.convertToTmsAttribute(projectId, key);

    var savedAttribute = tmsAttributeRepository.save(attribute);
    return savedAttribute.getId();
  }

  private TmsAttribute findAttributeByIdAndProjectId(Long projectId, Long attributeId) {
    return tmsAttributeRepository
        .findByIdAndProject_Id(attributeId, projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "TMS Attribute with id '" + attributeId + "' in project '" + projectId
                + "'"));
  }

  private void validateUniqueness(Long projectId, String key, String value) {
    if (tmsAttributeRepository.existsByProjectIdAndKeyAndValue(projectId, key, value)) {
      throw new EntityExistsException(
          "TMS Attribute with key '" + key + "' and value '" + value
              + "' already exists in project '" + projectId + "'");
    }
  }
}
