package com.epam.reportportal.base.core.tms.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchExecutionStatisticRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.reporting.RetentionPolicy;
import org.junit.jupiter.api.Test;

class TmsManualLaunchMapperTest {

  @Test
  void convert_exposesRetentionPolicyAndHidesSystemAttributes() {
    var launch = new Launch();
    launch.setLaunchType(LaunchTypeEnum.MANUAL);
    launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
    launch.getAttributes().add(new LaunchAttribute("retentionPolicy", "important", true));
    launch.getAttributes().add(new LaunchAttribute("team", "qa", false));

    var result = new TmsManualLaunchMapperImpl()
        .convert(launch, new TmsManualLaunchExecutionStatisticRS());

    assertEquals(RetentionPolicy.IMPORTANT, result.getRetentionPolicy());
    assertEquals(1, result.getAttributes().size());
    var attribute = result.getAttributes().iterator().next();
    assertEquals("team", attribute.getKey());
    assertEquals("qa", attribute.getValue());
    assertTrue(result.getAttributes().stream()
        .noneMatch(item -> "retentionPolicy".equals(item.getKey())));
  }
}
