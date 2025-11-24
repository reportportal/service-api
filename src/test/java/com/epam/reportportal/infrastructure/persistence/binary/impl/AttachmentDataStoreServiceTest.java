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

package com.epam.reportportal.infrastructure.persistence.binary.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class AttachmentDataStoreServiceTest extends BaseMvcTest {

  @Autowired
  private AttachmentDataStoreService attachmentDataStoreService;

  @Value("${datastore.path:/data/store}")
  private String storageRootPath;

  @Value("${datastore.bucketPrefix:prj-}")
  private String bucketPrefix;

  @Value("${datastore.bucketPostfix:}")
  private String bucketPostfix;

  private static final String BUCKET_NAME = "bucket";

  private static Random random = new Random();

  @Test
  void saveLoadAndDeleteTest() throws IOException {
    InputStream inputStream = new ClassPathResource("meh.png").getInputStream();

    String bucketPath = bucketPrefix + BUCKET_NAME + bucketPostfix;

    String fileId =
        attachmentDataStoreService.save(BUCKET_NAME + "/" + random.nextLong() + "meh.png",
            inputStream
        );

    Optional<InputStream> loadedData = attachmentDataStoreService.load(fileId);

    assertTrue(loadedData.isPresent());
    try (InputStream ignored = loadedData.get()) {
      String decodedPath = attachmentDataStoreService.dataEncoder.decode(fileId);
      decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
      assertTrue(Files.exists(Paths.get(storageRootPath, decodedPath)));
    }

    attachmentDataStoreService.delete(fileId);

    ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> attachmentDataStoreService.load(fileId));
    assertEquals("Unable to load binary data by id 'Unable to find file'", exception.getMessage());
    String decodedPath = attachmentDataStoreService.dataEncoder.decode(fileId);
    decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
    assertFalse(Files.exists(Paths.get(storageRootPath, decodedPath)));
  }

  @Test
  void saveLoadAndDeleteThumbnailTest() throws IOException {
    try (InputStream inputStream = new ClassPathResource("meh.png").getInputStream()) {
      String bucketPath = bucketPrefix + BUCKET_NAME + bucketPostfix;

      String thumbnailId = attachmentDataStoreService.saveThumbnail(
          BUCKET_NAME + "/" + random.nextLong() + "thumbnail.jpg", inputStream);

      Optional<InputStream> loadedData = attachmentDataStoreService.load(thumbnailId);

      assertTrue(loadedData.isPresent());
      try (InputStream ignored = loadedData.get()) {
        String decodedPath = attachmentDataStoreService.dataEncoder.decode(thumbnailId);
        decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
        assertTrue(Files.exists(Paths.get(storageRootPath, decodedPath)));
      }

      attachmentDataStoreService.delete(thumbnailId);

      ReportPortalException exception = assertThrows(ReportPortalException.class,
          () -> attachmentDataStoreService.load(thumbnailId)
      );
      assertEquals(
          "Unable to load binary data by id 'Unable to find file'", exception.getMessage());
      String decodedPath = attachmentDataStoreService.dataEncoder.decode(thumbnailId);
      decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
      assertFalse(Files.exists(Paths.get(storageRootPath, decodedPath)));
    }
  }
}
