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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.ws.reporting.ParameterResource;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ParametersConverterTest {

  @Test
  void toResource() {
    final Parameter parameter = getParameter();
    final ParameterResource resource = ParametersConverter.TO_RESOURCE.apply(parameter);

    assertEquals(resource.getKey(), parameter.getKey());
    assertEquals(resource.getValue(), parameter.getValue());
  }

  @Test
  void toModel() {
    final ParameterResource resource = getResource();
    final Parameter parameter = ParametersConverter.TO_MODEL.apply(resource);

    assertEquals(parameter.getKey(), resource.getKey());
    assertEquals(parameter.getValue(), resource.getValue());
  }

  private static Parameter getParameter() {
    Parameter parameter = new Parameter();
    parameter.setKey("key");
    parameter.setValue("value");
    return parameter;
  }

  private static ParameterResource getResource() {
    ParameterResource resource = new ParameterResource();
    resource.setKey("key");
    resource.setValue("value");
    return resource;
  }
}