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

package com.epam.reportportal.base.infrastructure.commons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;

@Slf4j
public class TikaContentTypeResolver implements ContentTypeResolver {

  @Override
  public String detectContentType(byte[] data) {
    return detectContentType(new ByteArrayInputStream(data));
  }

  @Override
  public String detectContentType(InputStream data) {
    return detectContentType(TikaInputStream.get(data));
  }

  @Override
  public String detectContentType(Path data) {
    try {
      return detectContentType(TikaInputStream.get(data));
    } catch (IOException e) {
      log.error("Cannot read data stream", e);
      return MediaType.OCTET_STREAM.toString();
    }
  }

  private String detectContentType(TikaInputStream is) {
    try {
      AutoDetectParser parser = new AutoDetectParser();
      Detector detector = parser.getDetector();
      MediaType mediaType = detector.detect(is, new Metadata(), new ParseContext());
      return mediaType.toString();
    } catch (IOException e) {
      log.error("Cannot read data stream", e);
      return MediaType.OCTET_STREAM.toString();
    }
  }

}
