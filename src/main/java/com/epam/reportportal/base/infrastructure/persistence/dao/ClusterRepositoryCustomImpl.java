package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JClustersTestItem.CLUSTERS_TEST_ITEM;

import com.epam.reportportal.base.infrastructure.persistence.entity.cluster.Cluster;
import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.records.JClustersTestItemRecord;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Repository
public class ClusterRepositoryCustomImpl implements ClusterRepositoryCustom {

  private final DSLContext dsl;

  @Autowired
  public ClusterRepositoryCustomImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public int saveClusterTestItems(Cluster cluster, Set<Long> itemIds) {
    final InsertValuesStep2<JClustersTestItemRecord, Long, Long> insertQuery = dsl.insertInto(
            CLUSTERS_TEST_ITEM)
        .columns(CLUSTERS_TEST_ITEM.CLUSTER_ID, CLUSTERS_TEST_ITEM.ITEM_ID);

    itemIds.forEach(itemId -> insertQuery.values(cluster.getId(), itemId));

    return insertQuery.execute();
  }
}
