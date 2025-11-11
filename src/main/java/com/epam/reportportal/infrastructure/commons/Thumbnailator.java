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

import java.io.IOException;
import java.io.InputStream;

/**
 * Creates Thumbnails of images
 *
 * @author Andrei Varabyeu
 */
public interface Thumbnailator {

  /**
   * Creates thumbnail of provided image
   *
   * @param is - Image's InputStream
   * @return - Thumbnail's InputStream
   * @throws IOException IO exception
   */
  InputStream createThumbnail(InputStream is) throws IOException;

  /**
   * Creates thumbnail of provided image
   *
   * @param data Image to be Thumbnailed
   * @return Thumbnail of image
   * @throws IOException IO exception
   */
  byte[] createThumbnail(byte[] data) throws IOException;
}
