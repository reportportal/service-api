/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
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

package com.epam.reportportal.base.infrastructure.commons;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to easily wraps logic with try/catch blocks
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public final class Safe {

  private static final Logger LOGGER = LoggerFactory.getLogger(Safe.class);

  private final Action work;
  private final Consumer<Exception> errorCallback;

  private Safe(Action work, Consumer<Exception> errorCallback) {
    this.work = work;
    this.errorCallback = errorCallback;
  }

  /**
   * Executes action in try/catch block ignoring any errors
   *
   * @param action Action to be performed
   */
  public static void safe(Action action) {
    new Safe(action, null).perform();
  }

  /**
   * Executes action in try/catch block and performs callback in case of any error
   *
   * @param action        Action to be executed
   * @param errorCallback Error callback
   */
  public static void safe(Action action, Consumer<Exception> errorCallback) {
    new Safe(action, errorCallback).perform();
  }

  /**
   * Performs action
   */
  private void perform() {
    try {
      work.perform();
    } catch (Exception e) {
      if (null != errorCallback) {
        errorCallback.accept(e);
      } else {
        LOGGER.error("Exception appears in safe block, but not handled. Ignoring...", e);
      }

    }

  }

  @FunctionalInterface
  public interface Action {

    void perform() throws Exception;
  }
}
