package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRS;
import org.springframework.data.domain.Pageable;

public interface TmsAttributeService {

  TmsAttributeRS create(TmsAttributeRQ request);

  TmsAttributeRS patch(Long attributeId, TmsAttributeRQ request);

  com.epam.ta.reportportal.model.Page<TmsAttributeRS> getAll(Pageable pageable);

  TmsAttributeRS getById(Long attributeId);
}
