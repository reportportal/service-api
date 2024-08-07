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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.logging.HttpLoggingAspect;
import com.epam.ta.reportportal.core.logging.RabbitMessageLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Konstantin Antipin
 */
@Configuration
public class AspectConfig {

  @Bean
  @ConditionalOnProperty(name = "rp.requestLogging", havingValue = "true")
  HttpLoggingAspect httpLoggingAspect() {
    return new HttpLoggingAspect();
  }

  @Bean
  @ConditionalOnProperty(name = "rp.requestLogging", havingValue = "true")
  RabbitMessageLoggingAspect rabbitMessageLoggingAspect() {
    return new RabbitMessageLoggingAspect();
  }
}
