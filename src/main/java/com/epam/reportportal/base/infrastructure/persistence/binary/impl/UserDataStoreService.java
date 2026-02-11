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

import com.epam.reportportal.base.infrastructure.commons.Thumbnailator;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataEncoder;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStore;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service("userDataStoreService")
public class UserDataStoreService extends CommonDataStoreService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserDataStoreService.class);

  private final Thumbnailator thumbnailator;

  @Autowired
  public UserDataStoreService(DataStore dataStore, DataEncoder dataEncoder,
      @Qualifier("userPhotoThumbnailator") Thumbnailator thumbnailator) {
    super(dataStore, dataEncoder);
    this.thumbnailator = thumbnailator;
  }

  @Override
  public String saveThumbnail(String fileName, InputStream data) {
    try {
      return dataEncoder.encode(dataStore.save(fileName, thumbnailator.createThumbnail(data)));
    } catch (IOException e) {
      LOGGER.error("Thumbnail is not created for file [{}].", fileName, e);
    }
    return null;
  }
}
