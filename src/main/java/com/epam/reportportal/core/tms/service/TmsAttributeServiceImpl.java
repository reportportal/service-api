package com.epam.reportportal.core.tms.service;

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;

import com.epam.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.core.tms.mapper.TmsAttributeMapper;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.filterable.TmsAttributeFilterableRepository;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.ws.converter.PagedResourcesAssembler;
import jakarta.persistence.EntityExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
    validateKeyUniquenessInProject(projectId, request.getKey());

    var entity = tmsAttributeMapper.convertToTmsAttribute(request, projectId);

    var savedEntity = tmsAttributeRepository.save(entity);

    return tmsAttributeMapper.convertToTmsAttributeRS(savedEntity);
  }

  @Override
  @Transactional
  public TmsAttributeRS patch(Long projectId, Long attributeId, TmsAttributeRQ request) {
    var existingAttribute = findAttributeByIdAndProjectId(projectId, attributeId);

    if (Objects.nonNull(request.getKey())
        && !request.getKey().equals(existingAttribute.getKey())) {
      validateKeyUniquenessInProject(projectId, request.getKey());
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
        .apply(tmsAttributeFilterableRepository.findByProjectIdAndFilter(projectId, filter, pageable));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsAttributeRS getById(Long projectId, Long attributeId) {
    return tmsAttributeMapper.convertToTmsAttributeRS(
        findAttributeByIdAndProjectId(projectId, attributeId)
    );
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

  private void validateKeyUniquenessInProject(Long projectId, String key) {
    if (tmsAttributeRepository.existsByKeyAndProject_Id(key, projectId)) {
      throw new EntityExistsException(
          "TMS Attribute with key '" + key + "' already exists in project '" + projectId + "'");
    }
  }
}
