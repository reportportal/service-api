/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.rules.validation;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.trimMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Several tests for {@link Suppliers}
 *
 * @author Andrei Varabyeu
 */
public class SuppliersTest {

  @Test
  public void testSimpleSupplier() {
    String demoString = "demo string";
    String suppliedString = Suppliers.stringSupplier(demoString).get();
    assertEquals(demoString, suppliedString, "Initial and supplied strings are not equal");
  }

  @Test
  public void testFormattedSupplier() {
    String demoString = "here is {} parameter";
    String suppliedString = Suppliers.formattedSupplier(demoString, "first").get();
    assertEquals("here is first parameter",
        suppliedString, "Initial and supplied strings are not equal");
  }

  @Test
  public void checkNoParamsInMessage() {
    String demoString = "here is parameter {}";
    String suppliedString = Suppliers.formattedSupplier(demoString).get();
    assertThat("Incorrect message builder", suppliedString,
        Matchers.is("here is parameter"));
  }

  @Test
  public void checkParamsInMessage() {
    String demoString = "here is parameter {}";
    String suppliedString = Suppliers.formattedSupplier(demoString, ":hello world").get();
    assertThat("Incorrect message builder", suppliedString,
        Matchers.is("here is parameter :hello world"));
  }

  @Test
  public void trimMessageTest() {
    String errMessage = "Incorrect truncate";
    String toTrim = "hello world";

    assertEquals("hello worl", trimMessage(toTrim, 10), errMessage);
    assertEquals("hello world", trimMessage(toTrim, 15), errMessage);
    assertEquals("hello world", trimMessage(toTrim, 11), errMessage);
    assertEquals("hello", trimMessage(toTrim, 5), errMessage);
  }

  @Test
  public void trimFormattedSupplierMessageTest() {
    String demoString = "here is parameter {}";
    String suppliedString = Suppliers.formattedSupplier(demoString, () -> "param").get();
    assertThat("Incorrect message builder", suppliedString,
        Matchers.is("here is parameter param"));
  }

}
