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

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Content type resolver
 *
 * @author Andrei Varabyeu
 */
public interface ContentTypeResolver {

  /**
   * Detects content type of data in input stream
   *
   * @param data Data to be resolved
   * @return detected media type, or <code>application/octet-stream</code>
   */
  String detectContentType(byte[] data);

  /**
   * Detects content type of data in input stream
   *
   * @param data Data to be resolved
   * @return detected media type, or <code>application/octet-stream</code>
   */
  String detectContentType(InputStream data);

  /**
   * Detects content type of data in file
   *
   * @param data Data to be resolved
   * @return detected media type, or <code>application/octet-stream</code>
   */
  String detectContentType(Path data);

}
