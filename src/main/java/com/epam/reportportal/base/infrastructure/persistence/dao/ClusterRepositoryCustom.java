package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.cluster.Cluster;
import java.util.Set;

/**
 * JOOQ helpers to persist cluster to test item links.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ClusterRepositoryCustom {

  int saveClusterTestItems(Cluster cluster, Set<Long> itemIds);
}
