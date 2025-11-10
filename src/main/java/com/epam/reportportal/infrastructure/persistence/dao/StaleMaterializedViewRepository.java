package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.materialized.StaleMaterializedView;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface StaleMaterializedViewRepository {

  Optional<StaleMaterializedView> findById(Long id);

  StaleMaterializedView insert(StaleMaterializedView view);
}
