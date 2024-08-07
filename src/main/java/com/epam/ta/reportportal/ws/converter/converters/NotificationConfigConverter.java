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

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.project.email.SenderCaseOptions;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class NotificationConfigConverter {

  private NotificationConfigConverter() {
    //static only
  }

  public final static Function<Set<SenderCase>, List<SenderCaseDTO>> TO_RESOURCE =
      senderCaseSet -> senderCaseSet.stream().map(NotificationConfigConverter.TO_CASE_RESOURCE)
          .collect(Collectors.toList());

  public static final Function<LaunchAttributeRule, ItemAttributeResource>
      TO_ATTRIBUTE_RULE_RESOURCE = model -> {
    ItemAttributeResource attributeResource = new ItemAttributeResource();
    attributeResource.setKey(model.getKey());
    attributeResource.setValue(model.getValue());
    return attributeResource;
  };

  public final static Function<SenderCase, SenderCaseDTO> TO_CASE_RESOURCE = model -> {
    Preconditions.checkNotNull(model);
    SenderCaseDTO resource = new SenderCaseDTO();
    resource.setLaunchNames(Lists.newArrayList(model.getLaunchNames()));
    ofNullable(model.getLaunchAttributeRules()).ifPresent(
        launchAttributeRules -> resource.setAttributes(
            launchAttributeRules.stream().map(TO_ATTRIBUTE_RULE_RESOURCE)
                .collect(Collectors.toSet())));
    resource.setSendCase(model.getSendCase().getCaseString());
    ofNullable(model.getRecipients()).ifPresent(
        recipients -> resource.setRecipients(Lists.newArrayList(recipients)));
    resource.setEnabled(model.isEnabled());
    resource.setAttributesOperator(model.getAttributesOperator().getOperator());
    resource.setRuleName(model.getRuleName());
    resource.setId(model.getId());
    resource.setType(model.getType());
    ofNullable(model.getRuleDetails()).map(SenderCaseOptions::getOptions)
        .ifPresent(resource::setRuleDetails);
    return resource;
  };

  public static final Function<ItemAttributeResource, LaunchAttributeRule> TO_ATTRIBUTE_RULE_MODEL =
      resource -> {
        LaunchAttributeRule launchAttributeRule = new LaunchAttributeRule();
        cutAttributeToMaxLength(resource);
        launchAttributeRule.setKey(resource.getKey());
        launchAttributeRule.setValue(resource.getValue());
        return launchAttributeRule;
      };

  private static void cutAttributeToMaxLength(ItemAttributeResource entity) {
    String key = entity.getKey();
    String value = entity.getValue();
    if (key != null && key.length() > ValidationConstraints.MAX_ATTRIBUTE_LENGTH) {
      entity.setKey(key.trim().substring(0, ValidationConstraints.MAX_ATTRIBUTE_LENGTH));
    }
    if (value != null && value.length() > ValidationConstraints.MAX_ATTRIBUTE_LENGTH) {
      entity.setValue(value.trim().substring(0, ValidationConstraints.MAX_ATTRIBUTE_LENGTH));
    }
  }

  public final static Function<SenderCaseDTO, SenderCase> TO_CASE_MODEL = resource -> {
    SenderCase senderCase = new SenderCase();
    ofNullable(resource.getAttributes()).ifPresent(
        attributes -> senderCase.setLaunchAttributeRules(attributes.stream().map(attribute -> {
          LaunchAttributeRule launchAttributeRule = TO_ATTRIBUTE_RULE_MODEL.apply(attribute);
          launchAttributeRule.setSenderCase(senderCase);
          return launchAttributeRule;
        }).collect(Collectors.toSet())));
    ofNullable(resource.getLaunchNames()).ifPresent(
        launchNames -> senderCase.setLaunchNames(Sets.newHashSet(launchNames)));
    ofNullable(resource.getRecipients()).ifPresent(
        recipients -> senderCase.setRecipients(Sets.newHashSet(recipients)));
    senderCase.setSendCase(SendCase.findByName(resource.getSendCase()).orElseThrow(
        () -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Incorrect send case type " + resource.getSendCase()
        )));
    senderCase.setEnabled(resource.isEnabled());
    senderCase.setAttributesOperator(LogicalOperator.valueOf(resource.getAttributesOperator()));
    senderCase.setRuleName(resource.getRuleName());
    senderCase.setId(resource.getId());
    senderCase.setType(resource.getType());
    Optional.ofNullable(resource.getRuleDetails()).map(SenderCaseOptions::new)
        .ifPresent(senderCase::setRuleDetails);
    return senderCase;
  };
}
