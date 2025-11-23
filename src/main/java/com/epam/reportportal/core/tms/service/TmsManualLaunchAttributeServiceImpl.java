package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsManualLaunchAttributeRQ;
import com.epam.reportportal.core.tms.mapper.TmsManualLaunchAttributeMapper;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsManualLaunchAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Service implementation for managing TMS Manual Launch attributes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsManualLaunchAttributeServiceImpl implements TmsManualLaunchAttributeService {

  private final TmsManualLaunchAttributeRepository tmsManualLaunchAttributeRepository;
  private final TmsAttributeRepository tmsAttributeRepository;
  private final TmsManualLaunchAttributeMapper tmsManualLaunchAttributeMapper;

  @Override
  @Transactional
  public void createAttributes(Launch launch, Collection<TmsManualLaunchAttributeRQ> attributes) {
    if (CollectionUtils.isEmpty(attributes)) {
      log.debug("No attributes to create for launch: {}", launch.getId());
      return;
    }

    var launchAttributes = tmsManualLaunchAttributeMapper.convertToManualLaunchAttributes(
        attributes);
    launchAttributes.forEach(launchAttribute -> launchAttribute.setLaunch(launch));
    tmsManualLaunchAttributeRepository.saveAll(launchAttributes);

    log.info("Created {} attributes for launch: {}", launchAttributes.size(), launch.getId());
  }

  @Override
  @Transactional
  public void updateAttributes(Launch existingLaunch,
      Collection<TmsManualLaunchAttributeRQ> attributes) {
    log.debug("Patching attributes for launch: {}", existingLaunch.getId());

    if (attributes == null) {
      log.debug("No attributes to patch for launch: {}", existingLaunch.getId());
      return;
    }

    // Delete existing attributes
    deleteAllByLaunchId(existingLaunch.getId());

    // Create new attributes
    if (!attributes.isEmpty()) {
      createAttributes(existingLaunch, attributes);
    }

    log.info("Patched attributes for launch: {}", existingLaunch.getId());
  }

  @Override
  @Transactional
  public void deleteAllByLaunchId(Long launchId) {
    log.debug("Deleting all attributes for launch: {}", launchId);

    tmsManualLaunchAttributeRepository.deleteByLaunchId(launchId);

    log.info("Deleted all attributes for launch: {}", launchId);
  }
}
