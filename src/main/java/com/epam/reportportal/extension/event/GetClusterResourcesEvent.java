package com.epam.reportportal.extension.event;


import com.epam.reportportal.infrastructure.model.launch.cluster.ClusterInfoResource;
import java.util.Collection;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class GetClusterResourcesEvent extends EntityCollectionEvent<ClusterInfoResource> {

  private final Long launchId;

  public GetClusterResourcesEvent(Collection<ClusterInfoResource> entities, Long launchId) {
    super(entities);
    this.launchId = launchId;
  }

  public Long getLaunchId() {
    return launchId;
  }
}
