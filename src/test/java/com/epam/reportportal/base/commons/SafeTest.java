/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/commons
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.reportportal.base.commons;

import static com.epam.reportportal.base.infrastructure.commons.Safe.safe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link Safe}
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class SafeTest {

  @Test
  public void testHappy() {
    AtomicBoolean result = new AtomicBoolean(false);
    safe(() -> result.set(true));
    assertTrue(result.get(), "Action is not executed");
  }

  @Test
  public void testNoCallback() {
    safe(() -> {
      throw new Exception("hello");
    });
    //no errors should be here
  }

  @Test
  public void testWithCallback() {
    AtomicBoolean result = new AtomicBoolean(false);

    safe(() -> {
      throw new Exception("hello");
    }, e -> result.set(true));

    assertTrue(result.get(), "Callback is not executed");

  }

  @Test
  public void testWithCallbackAndError() {
    AtomicBoolean result = new AtomicBoolean(false);

    try {
      safe(() -> {
        throw new Exception("hello");
      }, e -> {
        throw new RuntimeException("wraps error", e);
      });
    } catch (RuntimeException e) {
      assertEquals("wraps error", e.getMessage(), "Incorrect message");
      return;
    }
    fail("Exception hasn't been thrown");

    assertTrue(result.get(), "Callback is not executed");

  }

}
