package com.epam.ta.reportportal.core.tms.service;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.tms.TmsAttribute;
import com.epam.ta.reportportal.dao.tms.TmsAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsAttributeMapper;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import jakarta.persistence.EntityExistsException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsAttributeServiceImpl implements TmsAttributeService {

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
  public com.epam.ta.reportportal.model.Page<TmsAttributeRS> getAll(Pageable pageable) {
    return PagedResourcesAssembler
        .pageConverter(tmsAttributeMapper::convertToTmsAttributeRS)
        .apply(tmsAttributeRepository.findAll(pageable));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsAttributeRS getById(Long attributeId) {
    return tmsAttributeMapper.convertToTmsAttributeRS(
        findAttributeById(attributeId)
    );
  }

  private TmsAttribute findAttributeById(Long attributeId) {
    return tmsAttributeRepository
        .findById(attributeId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "TMS Attribute with id '" + attributeId));
  }

  private void validateKeyUniqueness(String key) {
    if (tmsAttributeRepository.existsByKey(key)) {
      throw new EntityExistsException(
          "TMS Attribute with key '" + key + "' already exists");
    }
  }
}
