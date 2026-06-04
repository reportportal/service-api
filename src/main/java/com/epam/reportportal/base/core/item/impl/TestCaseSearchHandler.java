/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.item.impl;

import static com.epam.reportportal.base.ws.resolver.PagingHandlerMethodArgumentResolver.CUT_DEFAULT_OFFSET;
import static com.epam.reportportal.base.ws.resolver.PagingHandlerMethodArgumentResolver.CUT_DEFAULT_PAGE_SIZE;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.model.Page.PageMetadata;
import com.epam.reportportal.base.reporting.TestItemResource;
import com.epam.reportportal.base.ws.converter.converters.TestItemConverter;
import com.epam.reportportal.base.ws.converter.utils.ResourceUpdater;
import com.epam.reportportal.base.ws.converter.utils.ResourceUpdaterProvider;
import com.epam.reportportal.base.ws.converter.utils.item.content.TestItemUpdaterContent;
import jakarta.persistence.QueryTimeoutException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestCaseSearchHandler {

  private final TestItemRepository testItemRepository;

  private final List<ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource>> resourceUpdaterProviders;

  public Page<TestItemResource> searchTestItems(String namePart, String attribute,
      String statuses, Pageable pageable, MembershipDetails membershipDetails) {
    pageable = validateInputParameters(namePart, attribute, pageable);
    Slice<TestItem> result;
    try {
      if (hasText(attribute)) {
        result = searchByAttribute(attribute, statuses, pageable, membershipDetails);
      } else {
        result = searchByName(namePart, statuses, pageable, membershipDetails);
      }
    } catch (QueryTimeoutException e) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          "Please refine your search by providing a more specific or unique test case name / attribute.");
    }
    var resourceUpdaters = getResourceUpdaters(membershipDetails.getProjectId(), result.getContent());
    return new com.epam.reportportal.base.model.Page<>(result.stream().map(item -> {
      var testItemResource = TestItemConverter.TO_RESOURCE.apply(item);
      resourceUpdaters.forEach(updater -> updater.updateResource(testItemResource));
      return testItemResource;
    }).collect(toList()), new PageMetadata(result.getPageable().getPageNumber() + 1,
        result.getPageable().getPageSize(), result.hasNext()));
  }

  private Slice<TestItem> searchByName(String namePart, String statuses, Pageable pageable,
      MembershipDetails membershipDetails) {
    if (hasText(statuses)) {
      return testItemRepository.findTestItemsContainsNameAndStatuses(namePart,
          membershipDetails.getProjectId(), parseStatuses(statuses), pageable);
    }
    return testItemRepository.findTestItemsContainsName(namePart,
        membershipDetails.getProjectId(), pageable);
  }

  private Slice<TestItem> searchByAttribute(String attribute, String statuses, Pageable pageable,
      MembershipDetails projectDetails) {
    if (attribute.contains(":")) {
      var attributeSplit = attribute.split(":");
      if (hasText(statuses)) {
        return testItemRepository.findTestItemsByAttributeAndStatuses(projectDetails.getProjectId(),
            attributeSplit[0], attributeSplit[1], parseStatuses(statuses), pageable);
      }
      return testItemRepository.findTestItemsByAttribute(projectDetails.getProjectId(),
          attributeSplit[0], attributeSplit[1], pageable);
    } else {
      if (hasText(statuses)) {
        return testItemRepository.findTestItemsByAttributeAndStatuses(projectDetails.getProjectId(),
            attribute, parseStatuses(statuses), pageable);
      }
      return testItemRepository.findTestItemsByAttribute(projectDetails.getProjectId(), attribute,
          pageable);
    }
  }

  private List<String> parseStatuses(String statuses) {
    return Arrays.stream(statuses.split(",")).filter(StatusEnum::isPresent).collect(toList());
  }

  private Pageable validateInputParameters(String namePart, String attribute, Pageable pageable) {
    if (0 == pageable.getPageSize() || CUT_DEFAULT_PAGE_SIZE < pageable.getPageSize()) {
      pageable = PageRequest.of(pageable.getPageNumber(), CUT_DEFAULT_PAGE_SIZE,
          pageable.getSort());
    }
    if (pageable.getOffset() > CUT_DEFAULT_OFFSET) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Total amount must be lower or equals than " + CUT_DEFAULT_OFFSET);
    }
    if (!hasText(namePart) && !hasText(attribute)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Provide either 'filter.has.compositeAttribute' or 'filter.cnt.name'.");
    }
    if (hasText(namePart) && namePart.length() < 3) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Value of 'filter.cnt.name' must contains more than 2 symbols.");
    }
    return pageable;
  }

  private List<ResourceUpdater<TestItemResource>> getResourceUpdaters(Long projectId,
      List<TestItem> testItems) {
    return resourceUpdaterProviders.stream()
        .map(retriever -> retriever.retrieve(TestItemUpdaterContent.of(projectId, testItems)))
        .collect(toList());
  }

}
