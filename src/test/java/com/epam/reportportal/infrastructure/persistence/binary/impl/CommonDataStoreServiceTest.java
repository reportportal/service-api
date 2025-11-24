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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.infrastructure.persistence.filesystem.DataEncoder;
import com.epam.reportportal.ws.BaseMvcTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class CommonDataStoreServiceTest extends BaseMvcTest {

  @Autowired
  @Qualifier("userDataStoreService")
  private DataStoreService dataStoreService;

  @Autowired
  private DataEncoder dataEncoder;

  @Value("${datastore.path:/data/store}")
  private String storageRootPath;

  @Value("${datastore.bucketPrefix:prj-}")
  private String bucketPrefix;

  @Value("${datastore.bucketPostfix:}")
  private String bucketPostfix;
  private static final String BUCKET_NAME = "bucket";

  private String getModifiedPath(String originalPath) {
    String bucketPath = bucketPrefix + BUCKET_NAME + bucketPostfix;
    return originalPath.replace(BUCKET_NAME, bucketPath);
  }

  @Test
  void saveTest() throws IOException {
    MultipartFile multipartFile = getMultipartFile("meh.png");
    String fileId =
        dataStoreService.save(BUCKET_NAME + File.separator + multipartFile.getOriginalFilename(),
            multipartFile.getInputStream()
        );
    assertNotNull(fileId);
    String decodedPath = getModifiedPath(dataEncoder.decode(fileId));
    Path filePath = Paths.get(storageRootPath, decodedPath);
    assertTrue(filePath.toFile().exists(), "File " + filePath + " does not exist");
    dataStoreService.delete(fileId);
  }

  @Test
  void saveThumbnailTest() throws IOException {
    MultipartFile multipartFile = getMultipartFile("meh.png");
    String fileId = dataStoreService.saveThumbnail(
        BUCKET_NAME + File.separator + multipartFile.getOriginalFilename(),
        multipartFile.getInputStream()
    );
    assertNotNull(fileId);
    String decodedPath = getModifiedPath(dataEncoder.decode(fileId));
    Path filePath = Paths.get(storageRootPath, decodedPath);
    assertTrue(filePath.toFile().exists(), "File " + filePath + " does not exist");
    dataStoreService.delete(fileId);
  }

  @Test
  void saveAndLoadTest() throws IOException {
    MultipartFile multipartFile = getMultipartFile("meh.png");
    String fileId =
        dataStoreService.saveThumbnail(BUCKET_NAME + "/" + multipartFile.getOriginalFilename(),
            multipartFile.getInputStream()
        );

    Optional<InputStream> content = dataStoreService.load(fileId);

    assertTrue(content.isPresent());
    dataStoreService.delete(fileId);
  }

  @Test
  void saveAndDeleteTest() throws IOException {
    MultipartFile multipartFile = getMultipartFile("meh.png");
    Random random = new Random();
    String fileId = dataStoreService.save(
        BUCKET_NAME + "/" + random.nextLong() + "/" + multipartFile.getOriginalFilename(),
        multipartFile.getInputStream()
    );

    dataStoreService.delete(fileId);

    assertFalse(Files.exists(Paths.get(dataEncoder.decode(getModifiedPath(fileId)))));
  }

  public static MultipartFile getMultipartFile(String path) throws IOException {
    File file = new ClassPathResource(path).getFile();

    return new MockMultipartFile(path, new FileInputStream(file));
  }
}
