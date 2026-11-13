/*
 * Copyright 2019 EPAM Systems
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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported image MIME types for thumbnails and avatars.
 *
 * @author Dzmitry_Kavalets
 */
public enum ImageFormat {

  JPEG("JPEG"),
  PNG("PNG"),
  GIF("GIF");

  private String value;

  ImageFormat(String value) {
    this.value = value;
  }

  public static Optional<ImageFormat> fromValue(String value) {
    return Arrays.stream(ImageFormat.values())
        .filter(format -> format.value.equalsIgnoreCase(value)).findAny();
  }

  public static List<String> getValues() {
    return Arrays.stream(ImageFormat.values()).map(format -> format.value)
        .collect(Collectors.toList());
  }
}
