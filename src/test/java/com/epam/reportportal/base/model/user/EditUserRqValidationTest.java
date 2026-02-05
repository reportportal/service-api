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

package com.epam.reportportal.base.model.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class EditUserRqValidationTest {

  private final String regex = "(\\s*[\\pL0-9-_\\.]+\\s*)+";
  private final Pattern pattern = Pattern.compile(regex);

  private List<String> correctNames;
  private List<String> incorrectNames;

  @BeforeEach
  public void setUp() {
    correctNames = new ArrayList<>() {
      {
        add("J J");
        add("J");
        add("J_J_9_a-qwe www");
        add("A b c d e");
        add("John Murphy");
      }
    };

    incorrectNames = new ArrayList<>() {
      {
        add("  ");
        add("");
        add("A b C *");
        add(" * ");
      }
    };

  }

  @Test
  public void validateFullName() {
    for (String name : correctNames) {
      Matcher matcher = pattern.matcher(name);
      assertTrue(matcher.matches());
    }

    for (String name : incorrectNames) {
      Matcher matcher = pattern.matcher(name);
      assertFalse(matcher.matches());
    }
  }
}
