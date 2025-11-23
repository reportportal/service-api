package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsManualLaunchAttributeRQ;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import java.util.Collection;

public interface TmsManualLaunchAttributeService {

  void updateAttributes(Launch existingLaunch, Collection<TmsManualLaunchAttributeRQ> attributes);

  void deleteAllByLaunchId(Long launchId);

  void createAttributes(Launch launch, Collection<TmsManualLaunchAttributeRQ> attributes);
}
