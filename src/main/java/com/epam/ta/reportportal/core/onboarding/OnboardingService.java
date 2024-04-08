/*
 * Copyright 2021 EPAM Systems
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
package com.epam.ta.reportportal.core.onboarding;

import com.epam.ta.reportportal.dao.OnboardingRepository;
import com.epam.ta.reportportal.entity.onboarding.Onboarding;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * @author Antonov Maksim
 */
@Service
public class OnboardingService {

  private final OnboardingRepository onboardingRepository;
  private final ObjectMapper objectMapper;

  public OnboardingService(OnboardingRepository onboardingRepository, ObjectMapper objectMapper) {
    this.onboardingRepository = onboardingRepository;
    this.objectMapper = objectMapper;
  }

  public Object getOnboardingDataForPageIfAvailable(String page) {
    Onboarding onboarding = onboardingRepository.findAvailableOnboardingByPage(page);
    // possibly use another parsing flow for some onboarding page, for now only text to list of questions
    try {
      return (onboarding != null) ? objectMapper.readValue(onboarding.getData(),
          new TypeReference<List<Map<String, String>>>() {
          }) : null;
    } catch (JsonProcessingException e) {
      throw new ReportPortalException(ErrorType.UNCLASSIFIED_ERROR,
          "Unable to parse onboarding data: " + e.getMessage());
    }
  }
}
