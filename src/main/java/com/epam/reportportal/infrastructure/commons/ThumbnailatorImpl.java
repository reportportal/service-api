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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;

/**
 * Thumbnailator implementation using <a href="http://code.google.com/p/thumbnailator/">Thumbnailator</a> API
 *
 * @author Andrei Varabyeu
 */
public class ThumbnailatorImpl implements Thumbnailator {

  // 80
  private int width = 80;

  // 60
  private int height = 60;

  /**
   * With default sizes
   */
  public ThumbnailatorImpl() {

  }

  public ThumbnailatorImpl(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public InputStream createThumbnail(InputStream is) throws IOException {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Thumbnails.of(is).size(width, height).toOutputStream(baos);
      return new ByteArrayInputStream(baos.toByteArray());
    }
  }

  @Override
  public byte[] createThumbnail(byte[] data) throws IOException {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Thumbnails.of(new ByteArrayInputStream(data)).size(width, height)
          .toOutputStream(baos);
      return baos.toByteArray();
    }

  }
}
