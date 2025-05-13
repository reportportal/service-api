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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_OWNER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_FULL_NAME;

import com.epam.reportportal.api.UserApi;
import com.epam.reportportal.api.model.InstanceUserPage;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.core.filter.OrganizationsSearchCriteriaService;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserControllerNew extends BaseController implements UserApi {

  private final GetFileHandler getFileHandler;
  private final EditUserHandler editUserHandler;
  private final GetUserHandler getUserHandler;

  private final HttpServletRequest httpServletRequest;
  private final OrganizationsSearchCriteriaService searchCriteriaService;


  public UserControllerNew(GetFileHandler getFileHandler, EditUserHandler editUserHandler,
      GetUserHandler getUserHandler,
      HttpServletRequest httpServletRequest, OrganizationsSearchCriteriaService searchCriteriaService) {
    this.getFileHandler = getFileHandler;
    this.editUserHandler = editUserHandler;
    this.getUserHandler = getUserHandler;
    this.httpServletRequest = httpServletRequest;
    this.searchCriteriaService = searchCriteriaService;
  }

  // TODO: Postpone new endpoints
/*  @Override
  @PreAuthorize(IS_ADMIN)
  @Transactional(readOnly = true)
  public ResponseEntity<InstanceUserPage> getUsers(String excludeFields, Integer offset,
      Integer limit, Order order, String accept, String sort, String email, UUID uuid,
      String externalId, String fullName, InstanceRole instanceRole, AccountType accountType) {

    String[] excludeArray = excludeFields != null ? excludeFields.split(",") : new String[0];
    Filter filter = new DefaultUserFilter(email, uuid, externalId, fullName, instanceRole, accountType)
        .getFilter();
    var pageable = ControllerUtils.getPageable(sort, order, offset, limit);

    InstanceUserPage users = getUserHandler.getUsersExcluding(filter, pageable, excludeArray);
    return new ResponseEntity<>(users, HttpStatus.OK);

  }


  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<InstanceUser> getUsersMe(String excludeFields) {
    var user = getLoggedUser();
    InstanceUser instanceUser = getUserHandler.getCurrentUser(user);
    return new ResponseEntity<>(instanceUser, HttpStatus.OK);
  }*/


  @Transactional
  @Override
  @PreAuthorize(IS_ADMIN)
  public ResponseEntity<InstanceUserPage> postUsersSearches(String accept, SearchCriteriaRQ searchCriteriaRQ) {
    Filter filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, User.class);
    Pageable pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(searchCriteriaRQ.getSort()) ? searchCriteriaRQ.getSort() : CRITERIA_FULL_NAME,
        searchCriteriaRQ.getOrder() != null ? searchCriteriaRQ.getOrder().toString() : Direction.ASC.name(),
        searchCriteriaRQ.getOffset(),
        searchCriteriaRQ.getLimit());

    return ResponseEntity
        .ok(getUserHandler.getUsersExcluding(filter, pageable));
  }


  @Override
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

  @Override
  @Transactional
  @PreAuthorize(ALLOWED_TO_OWNER)
  public ResponseEntity<Void> postUsersUserIdAvatar(Long userId,
      @Parameter(name = "file")
      @RequestPart(value = "file") MultipartFile file) {

    editUserHandler.uploadPhoto(userId, file);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  @Transactional
  @PreAuthorize(ALLOWED_TO_OWNER)
  public ResponseEntity<Void> deleteUsersUserIdAvatar(Long userId) {
    editUserHandler.deletePhoto(userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
