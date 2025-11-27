package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsManualLaunchAttributeRQ;
import com.epam.reportportal.core.tms.mapper.TmsManualLaunchAttributeMapper;
import com.epam.reportportal.infrastructure.persistence.dao.ItemAttributeRepository;
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

  private final ItemAttributeRepository itemAttributeRepository;
  private final TmsManualLaunchAttributeMapper tmsManualLaunchAttributeMapper;
  private final TmsAttributeService tmsAttributeService;

  @Override
  @Transactional
  public void createAttributes(Launch launch, Collection<TmsManualLaunchAttributeRQ> attributeRQS) {
    if (CollectionUtils.isEmpty(attributeRQS)) {
      log.debug("No attributeRQS to create for launch: {}", launch.getId());
      return;
    }

    var tmsAttributes = tmsAttributeService.getAllByIds(
        attributeRQS.stream().map(TmsManualLaunchAttributeRQ::getId).toList()
    );
    var launchAttributes = tmsManualLaunchAttributeMapper.convertToManualLaunchAttributes(
        attributeRQS, tmsAttributes, launch);
    itemAttributeRepository.saveAll(launchAttributes);
    launch.setAttributes(launchAttributes);

    log.info("Created {} attributeRQS for launch: {}", launchAttributes.size(), launch.getId());
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

    itemAttributeRepository.deleteAllByLaunchIdAndSystem(launchId, false);

    log.info("Deleted all attributes for launch: {}", launchId);
  }
}
