package com.epam.reportportal.base.core.tms.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchExecutionStatisticRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
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

  @Test
  void convert_withoutTestPlan_returnsLaunchWithNullTestPlan() {
    var launch = new Launch();
    launch.setId(1L);
    launch.setName("Manual launch without plan");
    launch.setLaunchType(LaunchTypeEnum.MANUAL);

    var result = new TmsManualLaunchMapperImpl()
        .convert(launch, null, new User(), null);

    assertEquals(1L, result.getId());
    assertEquals("Manual launch without plan", result.getName());
    assertNull(result.getTestPlan());
    assertNull(result.getExecutionStatistic());
  }
}
