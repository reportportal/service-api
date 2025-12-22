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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
  @Transactional(readOnly = true)
  public Map<Long, TmsAttribute> getAllByIds(Long projectId, List<Long> ids) {
    return tmsAttributeRepository
        .findAllById(ids)
        .stream()
        .filter(attr -> attr.getProject().getId().equals(projectId))
        .collect(Collectors.toMap(TmsAttribute::getId, Function.identity()));
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
