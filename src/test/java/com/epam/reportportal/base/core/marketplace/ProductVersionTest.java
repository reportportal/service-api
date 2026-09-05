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

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * The running release, and what it does with a range it cannot read.
 */
class ProductVersionTest {

  private ch.qos.logback.classic.Logger logger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void captureLogs() {
    logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ProductVersion.class);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void releaseLogs() {
    logger.detachAppender(appender);
  }

  private List<String> warnings() {
    return appender.list.stream().filter(event -> event.getLevel() == Level.WARN)
        .map(ILoggingEvent::getFormattedMessage).toList();
  }

  @Test
  void anUnsetVersionIsUnknownAndSatisfiesNothing() {
    var unknown = new ProductVersion("   ");

    assertFalse(unknown.isKnown());
    assertNull(unknown.value());
    assertFalse(unknown.satisfies(">=25.1"));
  }

  @Test
  void anUnsetVersionIsReportedOnceAtStartup() {
    new ProductVersion(null).reportIfUnknown();

    assertEquals(1, warnings().size());
    assertTrue(warnings().get(0).contains("rp.product.version"));
  }

  @Test
  void aKnownVersionIsCheckedAgainstBothEndsOfTheRange() {
    assertTrue(new ProductVersion("25.2").satisfies(">=25.1, <26.0"));
    assertFalse(new ProductVersion("25.0").satisfies(">=25.1, <26.0"));
    assertFalse(new ProductVersion("26.0").satisfies(">=25.1, <26.0"));
  }

  @Test
  void aRangeThatCannotBeReadIsUnknownCompatibilityAndIsLogged() {
    var version = new ProductVersion("25.2");

    assertFalse(version.satisfies("latest"));

    assertEquals(1, warnings().size());
    assertTrue(warnings().get(0).contains("latest"));
  }

  @Test
  void anAbsentRangeIsNotCompatibilityAndIsNotWorthLogging() {
    // No declaration is the registry saying nothing, not the registry saying something broken.
    var version = new ProductVersion("25.2");

    assertFalse(version.satisfies(null));
    assertFalse(version.satisfies("  "));

    assertTrue(warnings().isEmpty());
  }
}
