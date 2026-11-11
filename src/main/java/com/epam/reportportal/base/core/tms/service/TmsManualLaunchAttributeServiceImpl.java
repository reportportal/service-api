package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.core.launch.attribute.AttributeHandler.RETENTION_POLICY_KEY;
import static com.epam.reportportal.base.ws.converter.converters.AttributeConverter.FROM_LAUNCH_RESOURCE;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
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

    boolean replacesRetentionPolicy = attributes.stream().anyMatch(this::isSystemRetentionPolicy);

    if (existingLaunch.getAttributes() != null) {
      var attributesToDelete = existingLaunch.getAttributes().stream()
          .filter(attribute -> isReplaced(attribute, replacesRetentionPolicy))
          .toList();
      if (!attributesToDelete.isEmpty()) {
        launchAttributeRepository.deleteAll(attributesToDelete);
        existingLaunch.getAttributes().removeAll(attributesToDelete);
      }
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

  private boolean isReplaced(LaunchAttribute attribute, boolean replacesRetentionPolicy) {
    if (!Boolean.TRUE.equals(attribute.isSystem())) {
      return true;
    }
    return replacesRetentionPolicy && RETENTION_POLICY_KEY.equalsIgnoreCase(attribute.getKey());
  }

  private boolean isSystemRetentionPolicy(ItemAttributesRQ attribute) {
    return attribute.isSystem() && RETENTION_POLICY_KEY.equalsIgnoreCase(attribute.getKey());
  }
}
