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

package com.epam.ta.reportportal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class MultipartFileUtilsTest {

  @Test
  void getMultipartFile() throws IOException {
    String path = "image/image.png";
    File expected = new ClassPathResource(path).getFile();
    MockMultipartFile file = (MockMultipartFile)MultipartFileUtils.getMultipartFile(path);
    assertEquals(expected.length(), file.getSize());
    assertEquals(expected.getName(), file.getName());
    assertEquals("image/png", file.getContentType());
    try (FileInputStream expectedStream = new FileInputStream(expected)) {
      assertTrue(IOUtils.contentEquals(expectedStream, file.getInputStream()));
    }
  }
}
