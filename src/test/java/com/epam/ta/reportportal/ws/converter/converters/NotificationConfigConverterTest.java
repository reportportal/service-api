/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class NotificationConfigConverterTest {

  @Test
  void toCaseModelNullTest() {
    assertThrows(
        NullPointerException.class, () -> NotificationConfigConverter.TO_CASE_MODEL.apply(null));
  }

  @Test
  void toResource() {
    final Set<SenderCase> senderCases = getSenderCases();
    List<SenderCaseDTO> resources = NotificationConfigConverter.TO_RESOURCE.apply(senderCases);

    assertEquals(resources.size(), senderCases.size());
  }

  @Test
  void toCaseResource() {
    final SenderCase senderCase = getCase();
    final SenderCaseDTO senderCaseDTO =
        NotificationConfigConverter.TO_CASE_RESOURCE.apply(senderCase);

    assertThat(senderCaseDTO.getRecipients()).containsExactlyInAnyOrderElementsOf(
        senderCase.getRecipients());
    //		assertThat(senderCaseDTO.getAttributes()).containsExactlyInAnyOrderElementsOf(senderCase.getLaunchAttributeRules());
    assertThat(senderCaseDTO.getLaunchNames()).containsExactlyInAnyOrderElementsOf(
        senderCase.getLaunchNames());
    assertEquals(senderCaseDTO.getSendCase(), senderCase.getSendCase().getCaseString());
  }

  @Test
  void toCaseModel() {
    final SenderCaseDTO caseDTO = getCaseDTO();
    final SenderCase senderCase = NotificationConfigConverter.TO_CASE_MODEL.apply(caseDTO);

    assertThat(senderCase.getRecipients()).containsExactlyInAnyOrderElementsOf(
        caseDTO.getRecipients());
    assertThat(senderCase.getLaunchNames()).containsExactlyInAnyOrderElementsOf(
        caseDTO.getLaunchNames());
    //		assertThat(senderCase.getLaunchAttributes()).containsExactlyInAnyOrderElementsOf(caseDTO.getAttributes());
    assertEquals(senderCase.getSendCase().getCaseString(), caseDTO.getSendCase());
    assertEquals(senderCase.isEnabled(), caseDTO.isEnabled());
  }

  private static Set<SenderCase> getSenderCases() {
    Set<SenderCase> senderCases = new HashSet<>();
    senderCases.add(getCase());
    final LaunchAttributeRule launchAttributeRule = new LaunchAttributeRule();
    launchAttributeRule.setId(1L);
    launchAttributeRule.setKey("key");
    launchAttributeRule.setValue("value");
    senderCases.add(new SenderCase("rule", Sets.newHashSet("recipent3", "recipient8"),
        Sets.newHashSet("launch1", "launch5", "launch10"), Sets.newHashSet(launchAttributeRule),
        SendCase.ALWAYS,  true, "email", LogicalOperator.AND
    ));
    return senderCases;
  }

  private static SenderCase getCase() {
    final LaunchAttributeRule launchAttributeRule = new LaunchAttributeRule();
    launchAttributeRule.setId(2L);
    launchAttributeRule.setKey("key1");
    launchAttributeRule.setValue("value1");
    return new SenderCase("rule", Sets.newHashSet("recipent1", "recipient2"),
        Sets.newHashSet("launch1", "launch2", "launch3"), Sets.newHashSet(launchAttributeRule),
        SendCase.MORE_10, true, "email", LogicalOperator.AND
    );
  }

  private static SenderCaseDTO getCaseDTO() {
    SenderCaseDTO senderCaseDTO = new SenderCaseDTO();
    senderCaseDTO.setRecipients(Arrays.asList("recipient1", "recipient2"));
    senderCaseDTO.setLaunchNames(Arrays.asList("launch1", "launch2"));
    final ItemAttributeResource launchAttribute = new ItemAttributeResource();
    launchAttribute.setKey("key");
    launchAttribute.setValue("val");
    senderCaseDTO.setAttributes(Sets.newHashSet(launchAttribute));
    senderCaseDTO.setSendCase("always");
    senderCaseDTO.setEnabled(true);
    senderCaseDTO.setAttributesOperator(LogicalOperator.AND.getOperator());
    return senderCaseDTO;
  }

}