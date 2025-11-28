package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.reporting.ItemAttributesRQ;
import java.util.Collection;

public interface TmsManualLaunchAttributeService {

  void updateAttributes(Launch existingLaunch, Collection<ItemAttributesRQ> attributes);

  void deleteAllByLaunchId(Long launchId);

  void createAttributes(Launch launch, Collection<ItemAttributesRQ> attributes);
}
