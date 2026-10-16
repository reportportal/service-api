package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.ws.converter.converters.AttributeConverter.FROM_LAUNCH_RESOURCE;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchAttributeRepository;
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

  private final LaunchAttributeRepository launchAttributeRepository;

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
          var launchAttribute = FROM_LAUNCH_RESOURCE.apply(attribute);
          launchAttribute.setLaunch(launch);
          return launchAttribute;
        })
        .collect(Collectors.toSet());

    launchAttributeRepository.saveAll(launchAttributes);


    if (launch.getAttributes() == null) {
      launch.setAttributes(launchAttributes);
    } else {
      launch.getAttributes().addAll(launchAttributes);
    }

    log.debug("Created {} attributeRQS for launch: {}", launchAttributes.size(), launch.getId());
  }

  @Override
  @Transactional
  public void updateAttributes(Launch existingLaunch, Collection<ItemAttributesRQ> attributes) {
    if (attributes == null) {
      log.debug("No attributes to update for launch: {}", existingLaunch.getId());
      return;
    }

    log.debug("Updating attributes for launch: {} with PUT semantics", existingLaunch.getId());


    if (existingLaunch.getAttributes() != null) {
      launchAttributeRepository.deleteAll(existingLaunch.getAttributes());
      existingLaunch.getAttributes().clear();
    }

    if (!attributes.isEmpty()) {
      createAttributes(existingLaunch, attributes);
    }

    log.debug("Updated attributes for launch: {}", existingLaunch.getId());
  }

  @Override
  @Transactional
  public void deleteAllByLaunchId(Long launchId) {
    log.debug("Deleting all non-system attributes for launch: {}", launchId);

    launchAttributeRepository.deleteAllByLaunchIdAndSystem(launchId, false);

    log.debug("Deleted all non-system attributes for launch: {}", launchId);
  }
}
