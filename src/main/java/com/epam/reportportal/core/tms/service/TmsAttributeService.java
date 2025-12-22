package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsAttributeRS;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public interface TmsAttributeService {

  TmsAttributeRS create(Long projectId, TmsAttributeRQ request);

  TmsAttributeRS patch(Long projectId, Long attributeId, TmsAttributeRQ request);

  Page<TmsAttributeRS> getAll(Long projectId, Filter filter, Pageable pageable);

  TmsAttributeRS getById(Long projectId, Long attributeId);

  Map<Long, TmsAttribute> getAllByIds(Long projectId, List<Long> list);
}
