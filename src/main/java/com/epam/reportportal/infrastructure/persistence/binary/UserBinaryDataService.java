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

package com.epam.reportportal.infrastructure.persistence.binary;

import com.epam.reportportal.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface UserBinaryDataService {

  void saveUserPhoto(User user, MultipartFile file);

  void saveUserPhoto(User user, BinaryData binaryData);

  void saveUserPhoto(User user, InputStream inputStream, String contentType);

  BinaryData loadUserPhoto(User user, boolean loadThumbnail);

  public void deleteUserPhoto(User user);
}
