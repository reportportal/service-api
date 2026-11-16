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

package com.epam.reportportal.base.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.commons.Thumbnailator;
import com.epam.reportportal.base.infrastructure.commons.ThumbnailatorImpl;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * Set of tests for Thumbnailator
 *
 * @author Andrei Varabyeu
 */
public class ThumbnailatorImplTest {

  private static final String TEST_FILE_NAME = "meh.png";

  private static final int THUMBNAIL_WIDTH = 71;

  private static final int THUMBNAIL_HEIGHT = 60;

  /**
   * Validates sizes of thumbnail
   *
   * @throws IOException
   */
  @Test
  public void checkThumbnailator() throws IOException {
    Thumbnailator thumbnailator = new ThumbnailatorImpl();
    InputStream is =
        thumbnailator.createThumbnail(this.getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME));

    BufferedImage image = ImageIO.read(is);

    assertEquals(THUMBNAIL_HEIGHT, image.getHeight(), "Incorrect image height");
    assertEquals(THUMBNAIL_WIDTH, image.getWidth(), "Incorrect image width");

  }
}
