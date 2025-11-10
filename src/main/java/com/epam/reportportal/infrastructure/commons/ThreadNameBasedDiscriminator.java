/*
 * Copyright 2016 EPAM Systems
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

package com.epam.reportportal.infrastructure.commons;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;

/**
 * Adds possibility to have per thread log files in logback
 *
 * @author Andrei Varabyeu
 */
public class ThreadNameBasedDiscriminator implements Discriminator<ILoggingEvent> {

  private static final String KEY = "threadName";

  private boolean started;

  @Override
  public String getDiscriminatingValue(ILoggingEvent iLoggingEvent) {
    return Thread.currentThread().getName();
  }

  @Override
  public String getKey() {
    return KEY;
  }

  @Override
  public void start() {
    started = true;
  }

  @Override
  public void stop() {
    started = false;
  }

  @Override
  public boolean isStarted() {
    return started;
  }
}
