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

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.util.BinaryDataResponseWriter;
import jakarta.activation.MimetypesFileTypeMap;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tuning for multipart file uploads in the service.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class MultipartDataConfig {

  @Bean
  public MimetypesFileTypeMap mimetypesFileTypeMap() {
    return new MimetypesFileTypeMap();
  }

  @Bean
  public AutoDetectParser autoDetectParser() {
    return new AutoDetectParser();
  }

  @Bean
  public BinaryDataResponseWriter binaryDataResponseWriter() {
    return new BinaryDataResponseWriter();
  }
}
