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

package com.epam.ta.reportportal.ws.resolver;

/**
 * Wrapper for java bean to be aware about mapped json view mapped to it
 *
 * @author Andrei Varabyeu
 */
public class JacksonViewAware {

  /*
   * Java bean to be wrapped
   */
  private final Object pojo;

  /*
   * Jackson's JSON View
   */
  private final Class<?> view;

  public JacksonViewAware(Object pojo, Class<?> view) {
    this.pojo = pojo;
    this.view = view;
  }

  public Object getPojo() {
    return pojo;
  }

  public Class<?> getView() {
    return view;
  }

}