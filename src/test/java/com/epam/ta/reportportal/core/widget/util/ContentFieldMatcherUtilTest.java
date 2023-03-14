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

package com.epam.ta.reportportal.core.widget.util;

import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.COMBINED_CONTENT_FIELDS_REGEX;
import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.DEFECTS_REGEX;
import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.EXECUTIONS_REGEX;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
//TODO replace random wrong content field generation with the specified loop
public class ContentFieldMatcherUtilTest {

  public static Object[][] data() {
    return new Object[1][0];
  }

  @Test
  void whenCorrectDefectsContentFieldsFormatThenTrue() {

    boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, buildCorrectDefectContentFields());

    assertTrue(match);
  }

  @Test
  void whenWrongDefectsContentFieldsFormatThenFalse() {

    boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, buildWrongDefectContentFields());

    assertFalse(match);
  }

  @Test
  void whenCorrectExecutionsContentFieldsFormatThenTrue() {

    boolean match = ContentFieldMatcherUtil.match(EXECUTIONS_REGEX,
        buildCorrectExecutionContentFields());

    assertTrue(match);
  }

  @Test
  void whenWrongExecutionsContentFieldsFormatThenFalse() {

    List<String> contentFields = buildWrongExecutionContentFields();
    boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, contentFields);

    assertFalse(match);
  }

  @Test
  void whenCorrectCombinedContentFieldsFormatThenTrue() {

    boolean match = ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX,
        buildCorrectCombinedContentFields());

    assertTrue(match);
  }

  @Test
  void whenWrongCombinedContentFieldsFormatThenFalse() {

    boolean match = ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX,
        buildWrongCombinedContentFields());

    assertFalse(match);
  }

  private List<String> buildCorrectDefectContentFields() {
    return Lists.newArrayList(
        "statistics$defects$automation_bug$AB001",
        "statistics$defects$product_bug$PB001",
        "statistics$defects$to_investigate$TI001",
        "statistics$defects$system_issue$SI001",
        "statistics$defects$no_defect$ND001",
        "statistics$defects$no_defect$total",
        "statistics$defects$product_bug$total",
        "statistics$defects$to_investigate$total",
        "statistics$defects$system_issue$total"

    );
  }

  private List<String> buildWrongDefectContentFields() {
    List<String> contentFields = buildCorrectDefectContentFields();
    Random random = new Random();
    int index = random.nextInt(contentFields.size());

    contentFields.set(index, "statistics$wrong$format");

    return contentFields;
  }

  private List<String> buildCorrectExecutionContentFields() {
    return Lists.newArrayList(
        "statistics$executions$passed",
        "statistics$executions$failed",
        "statistics$executions$skipped",
        "statistics$executions$total"
    );
  }

  private List<String> buildWrongExecutionContentFields() {
    List<String> contentFields = buildCorrectExecutionContentFields();

    Random random = new Random();
    int index = random.nextInt(contentFields.size());

    contentFields.set(index, "statistics$wrong$format");

    return contentFields;
  }

  private List<String> buildCorrectCombinedContentFields() {
    return Lists.newArrayList(
        "statistics$executions$passed",
        "statistics$executions$failed",
        "statistics$executions$skipped",
        "statistics$executions$total",
        "statistics$defects$automation_bug$AB001",
        "statistics$defects$product_bug$PB001",
        "statistics$defects$to_investigate$TI001",
        "statistics$defects$system_issue$SI001",
        "statistics$defects$no_defect$ND001",
        "statistics$defects$no_defect$total"
    );
  }

  private List<String> buildWrongCombinedContentFields() {
    List<String> contentFields = buildCorrectCombinedContentFields();

    Random random = new Random();
    int index = random.nextInt(contentFields.size());

    contentFields.set(index, "statistics$wrong$format");

    return contentFields;
  }

}