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
package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.core.onboarding.OnboardingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Antonov Maksim
 */
@RestController
@RequestMapping("/v1/onboarding")
public class OnboardingController {

  private final OnboardingService onboardingService;

  public OnboardingController(OnboardingService onboardingService) {
    this.onboardingService = onboardingService;
  }

  /**
   * Provide unstructured onboarding information. Possible json or string(html, js, etc), or
   * something else.
   */
  @GetMapping(value = {"", "/"})
  @ApiOperation("Return onboarding information for page if available, -1 otherwise")
  public Object onBoarding(@RequestParam(value = "page", defaultValue = "GENERAL") String page) {
    // object because it can be different types of onboarding data
    Object data = onboardingService.getOnboardingDataForPageIfAvailable(page);
    return (data != null) ? data : -1;
  }
}
