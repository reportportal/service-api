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

package com.epam.reportportal.base.infrastructure.persistence.binary.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.binary.impl.UserDataStoreService;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
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
class UserDataStoreServiceTest extends BaseMvcTest {

  @Autowired
  private UserDataStoreService userDataStoreService;

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
        userDataStoreService.save(BUCKET_NAME + "/" + random.nextLong() + "meh.png", inputStream);

    Optional<InputStream> loadedData = userDataStoreService.load(fileId);

    assertTrue(loadedData.isPresent());
    try (InputStream ignored = loadedData.get()) {
      String decodedPath = userDataStoreService.dataEncoder.decode(fileId);
      decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
      assertTrue(Files.exists(Paths.get(storageRootPath, decodedPath)));
    }

    userDataStoreService.delete(fileId);

    ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> userDataStoreService.load(fileId));
    assertEquals("Unable to load binary data by id 'Unable to find file'", exception.getMessage());
    String decodedPath = userDataStoreService.dataEncoder.decode(fileId);
    decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
    assertFalse(Files.exists(Paths.get(storageRootPath, decodedPath)));
  }

  @Test
  void saveLoadAndDeleteThumbnailTest() throws IOException {
    InputStream inputStream = new ClassPathResource("meh.png").getInputStream();

    String bucketPath = bucketPrefix + BUCKET_NAME + bucketPostfix;

    String thumbnailId =
        userDataStoreService.saveThumbnail(BUCKET_NAME + "/" + random.nextLong() + "thmbnail.jpg",
            inputStream
        );

    Optional<InputStream> loadedData = userDataStoreService.load(thumbnailId);

    assertTrue(loadedData.isPresent());
    try (InputStream ignored = loadedData.get()) {
      String decodedPath = userDataStoreService.dataEncoder.decode(thumbnailId);
      decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
      assertTrue(Files.exists(Paths.get(storageRootPath, decodedPath)));
    }

    userDataStoreService.delete(thumbnailId);

    ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> userDataStoreService.load(thumbnailId));
    assertEquals("Unable to load binary data by id 'Unable to find file'", exception.getMessage());
    String decodedPath = userDataStoreService.dataEncoder.decode(thumbnailId);
    decodedPath = decodedPath.replace(BUCKET_NAME, bucketPath);
    assertFalse(Files.exists(Paths.get(storageRootPath, decodedPath)));
  }
}
