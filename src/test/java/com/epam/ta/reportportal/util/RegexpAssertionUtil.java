/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.util;

import com.epam.reportportal.api.model.ProjectDetails;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import javax.validation.constraints.Pattern;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;


@Log4j2
public class RegexpAssertionUtil {

  private RegexpAssertionUtil() {
  }

  public static void checkRegexpPattern(String method, String value) throws NoSuchMethodException {
    Method getSlugMethod = ProjectDetails.class.getMethod(method);
    Pattern patternAnnotation = getSlugMethod.getAnnotation(Pattern.class);
    String regexp = patternAnnotation.regexp();

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regexp);
    Matcher matcher = pattern.matcher(value);

    log.info("Value: '{}')", value);
    Assertions.assertTrue(matcher.matches(),
        String.format("Value '%s' does not match the pattern", value));

  }
}
