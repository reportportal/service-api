/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.AUTHENTICATED;

import com.epam.reportportal.api.UserApi;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController extends BaseController implements UserApi {

  private final GetFileHandler getFileHandler;

  public UserController(GetFileHandler getFileHandler) {
    this.getFileHandler = getFileHandler;
  }

  @Override
  @PreAuthorize(AUTHENTICATED)
  @Transactional(readOnly = true)
  public ResponseEntity<Resource> getUsersUserIdAvatar(Long userId, Boolean thumbnail) {
    var binaryData = getFileHandler.getUserPhoto(userId, thumbnail);
    Resource resource = new InputStreamResource(binaryData.getInputStream());

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(binaryData.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + binaryData.getFileName() + "\"")
        .body(resource);
  }
}
