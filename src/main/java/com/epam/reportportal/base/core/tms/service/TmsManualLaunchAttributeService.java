package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.reporting.AttributesRQ;
import java.util.Collection;

public interface TmsManualLaunchAttributeService {

  void updateAttributes(Launch existingLaunch, Collection<AttributesRQ> attributes);

  void deleteAllByLaunchId(Long launchId);

  void createAttributes(Launch launch, Collection<AttributesRQ> attributes);
}
