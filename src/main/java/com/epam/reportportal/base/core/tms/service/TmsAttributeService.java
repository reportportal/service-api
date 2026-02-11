package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRS;
import org.springframework.data.domain.Pageable;

public interface TmsAttributeService {

  TmsAttributeRS create(TmsAttributeRQ request);

  TmsAttributeRS patch(Long attributeId, TmsAttributeRQ request);

  Page<TmsAttributeRS> getAll(Filter filter, Pageable pageable);

  TmsAttributeRS getById(Long attributeId);
}
