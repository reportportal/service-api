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

package com.epam.reportportal.base.infrastructure.persistence.filesystem.tms;

import java.io.InputStream;
import java.util.List;

/**
 * Contract for storing TMS file attachments.
 *
 * @author Dzianis_Shybeka
 */
public interface TmsDataStore {

  String save(String fileName, InputStream inputStream);

  InputStream load(String filePath);

  boolean exists(String filePath);

  void delete(String filePath);

  void deleteAll(List<String> filePaths, String bucketName);

  void deleteContainer(String bucketName);
}
