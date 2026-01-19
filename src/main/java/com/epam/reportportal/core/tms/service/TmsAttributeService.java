package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsAttributeRS;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface TmsAttributeService {

  TmsAttributeRS create(Long projectId, TmsAttributeRQ request);

  TmsAttributeRS patch(Long projectId, Long attributeId, TmsAttributeRQ request);

  Page<TmsAttributeRS> getAll(Long projectId, Filter filter, Pageable pageable);

  TmsAttributeRS getById(Long projectId, Long attributeId);

  /**
   * Resolves attribute keys for multiple test cases efficiently.
   *
   * @param projectId        the project ID
   * @param allAttributeKeys all attribute keys from all rows (flattened)
   * @return map of attribute key to attribute ID
   */
  Map<String, Long> resolveAttributes(Long projectId, Set<String> allAttributeKeys);
}
