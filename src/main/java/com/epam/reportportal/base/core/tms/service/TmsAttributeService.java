package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRS;
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

  // Get entity by ID (for internal use in services)
  TmsAttribute getEntityById(Long projectId, Long attributeId);
  
  // Find or create tag (value=NULL) for TestCase
  TmsAttribute findOrCreateTag(Long projectId, String key);
  
  // Find or create attribute with value for TestPlan/ManualScenario
  TmsAttribute findOrCreateAttribute(Long projectId, String key, String value);

  List<String> getKeysByCriteria(Long projectId, String search);

  List<String> getValuesByCriteria(Long projectId, String search);
}
