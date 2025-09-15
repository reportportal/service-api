/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.project.email.SenderCaseOptions;
import com.epam.ta.reportportal.model.activity.NotificationRuleActivityResource;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class NotificationRuleConverter {

  private NotificationRuleConverter() {
  }

  public static final Function<SenderCase, NotificationRuleActivityResource> TO_ACTIVITY_RESOURCE =
      senderCase -> {
        NotificationRuleActivityResource resource = new NotificationRuleActivityResource();
        resource.setId(senderCase.getId());
        resource.setName(senderCase.getRuleName());
        resource.setProjectId(senderCase.getProject().getId());
        resource.setEnabled(senderCase.isEnabled());
        resource.setType(senderCase.getType());
        resource.setAttributesOperator(senderCase.getAttributesOperator().getOperator());
        resource.setSendCase(senderCase.getSendCase().getCaseString());

        ofNullable(senderCase.getRecipients()).ifPresent(rcp -> resource.setRecipients(new ArrayList<>(rcp)));
        ofNullable(senderCase.getLaunchNames()).ifPresent(ln -> resource.setLaunchNames(new ArrayList<>(ln)));
        ofNullable(senderCase.getLaunchAttributeRules())
            .ifPresent(attrs -> resource.setAttributes(attrs.stream()
                .map(NotificationConfigConverter.TO_ATTRIBUTE_RULE_RESOURCE)
                .collect(Collectors.toSet())));
        ofNullable(senderCase.getRuleDetails()).map(SenderCaseOptions::getOptions)
            .ifPresent(resource::setRuleDetails);
        return resource;
      };
}
