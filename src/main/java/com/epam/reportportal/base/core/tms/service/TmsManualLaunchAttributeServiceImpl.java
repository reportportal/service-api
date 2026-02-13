package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.ws.converter.converters.ItemAttributeConverter.FROM_RESOURCE;

import com.epam.reportportal.base.infrastructure.persistence.dao.ItemAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import java.util.Collection;
import java.util.stream.Collectors;
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

  @Override
  @Transactional
  public void createAttributes(Launch launch, Collection<ItemAttributesRQ> attributeRQS) {
    if (CollectionUtils.isEmpty(attributeRQS)) {
      log.debug("No attributeRQS to create for launch: {}", launch.getId());
      return;
    }

    var launchAttributes = attributeRQS
        .stream()
        .map(attribute -> {
          var itemAttribute = FROM_RESOURCE.apply(attribute);
          itemAttribute.setLaunch(launch);
          return itemAttribute;
        })
        .collect(Collectors.toSet());

    itemAttributeRepository.saveAll(launchAttributes);

    launch.setAttributes(launchAttributes);

    log.debug("Created {} attributeRQS for launch: {}", launchAttributes.size(), launch.getId());
  }

  @Override
  @Transactional
  public void updateAttributes(Launch existingLaunch,
      Collection<ItemAttributesRQ> attributes) {
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

    log.debug("Patched attributes for launch: {}", existingLaunch.getId());
  }

  @Override
  @Transactional
  public void deleteAllByLaunchId(Long launchId) {
    log.debug("Deleting all attributes for launch: {}", launchId);

    itemAttributeRepository.deleteAllByLaunchIdAndSystem(launchId, false);

    log.debug("Deleted all attributes for launch: {}", launchId);
  }
}
