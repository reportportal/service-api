/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.ta.reportportal.ws.converter.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.model.activity.NotificationRuleActivityResource;
import org.junit.jupiter.api.Test;

class NotificationRuleConverterTest {

  @Test
  void toActivityResourceWhenSenderCaseProvidedShouldMapAllFields() {
    // given
    Project project = new Project();
    project.setId(5L);

    SenderCase senderCase = new SenderCase();
    senderCase.setId(10L);
    senderCase.setProject(project);
    senderCase.setRuleName("rule-1");
    senderCase.setAttributesOperator(LogicalOperator.AND);
    senderCase.setSendCase(SendCase.ALWAYS);

    // when
    NotificationRuleActivityResource resource = NotificationRuleConverter.TO_ACTIVITY_RESOURCE.apply(senderCase);
    // then
    assertEquals(10L, resource.getId());
    assertEquals(5L, resource.getProjectId());
    assertEquals("rule-1", resource.getName());
  }
}
