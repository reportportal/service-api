package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.materialized.StaleMaterializedView;
import java.util.Optional;

/**
 * Flags materialized views that need a refresh job.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface StaleMaterializedViewRepository {

  Optional<StaleMaterializedView> findById(Long id);

  StaleMaterializedView insert(StaleMaterializedView view);
}
