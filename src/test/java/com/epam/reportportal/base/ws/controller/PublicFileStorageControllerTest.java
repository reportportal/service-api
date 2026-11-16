/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.ws.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.file.FileSignedLinkService;
import com.epam.reportportal.base.infrastructure.persistence.binary.AttachmentBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.commons.BinaryDataMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.AttachmentMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;

@Sql("/db/data-store/data-store-fill.sql")
class PublicFileStorageControllerTest extends BaseMvcTest {

  private static final String PROJECT_KEY = "superadmin_personal";
  private static final byte[] FILE_CONTENT = createFileContent();

  @Autowired
  private AttachmentBinaryDataService attachmentBinaryDataService;

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private FileSignedLinkService fileSignedLinkService;

  @Autowired
  private UserRepository userRepository;

  @Value("${rp.jwt.signing-key}")
  private String signingKey;

  @Test
  void shouldCreateSignedStreamLinkForAuthorizedUser() throws Exception {
    var attachment = saveAttachment();

    mockMvc.perform(post("/v1/data/{projectKey}/streams/{dataId}/link",
            PROJECT_KEY,
            attachment.getId()
        ).with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.url",
            containsString("/v1/public/data/streams/" + attachment.getId())))
        .andExpect(jsonPath("$.url", containsString("sig=")))
        .andExpect(jsonPath("$.expiresAt").exists());
  }

  @Test
  void shouldStreamFullAttachmentWithoutAuthorizationHeader() throws Exception {
    var attachment = saveAttachment();
    var userId = findUserId("admin@reportportal.internal");
    var url = createSignedUrl(attachment.getId(), userId);

    performStream(url)
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
        .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, FILE_CONTENT.length))
        .andExpect(content().bytes(FILE_CONTENT));
  }

  @Test
  void shouldStreamRequestedAttachmentRangeWithoutAuthorizationHeader() throws Exception {
    var attachment = saveAttachment();
    var userId = findUserId("admin@reportportal.internal");
    var url = createSignedUrl(attachment.getId(), userId);

    performStream(url, "bytes=0-1023")
        .andExpect(status().isPartialContent())
        .andExpect(header().string(
            HttpHeaders.CONTENT_RANGE,
            "bytes 0-1023/" + FILE_CONTENT.length
        ))
        .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 1024))
        .andExpect(content().bytes(java.util.Arrays.copyOfRange(FILE_CONTENT, 0, 1024)));
  }

  @Test
  void shouldRejectRangeOutsideAttachmentWithoutReadingBody() throws Exception {
    var attachment = saveAttachment();
    var userId = findUserId("admin@reportportal.internal");
    var url = createSignedUrl(attachment.getId(), userId);

    mockMvc.perform(get(url).header(HttpHeaders.RANGE, "bytes=4096-"))
        .andExpect(status().isRequestedRangeNotSatisfiable())
        .andExpect(header().string(
            HttpHeaders.CONTENT_RANGE,
            "bytes */" + FILE_CONTENT.length
        ))
        .andExpect(content().bytes(new byte[0]));
  }

  @Test
  void shouldReturnUnauthorizedForExpiredToken() throws Exception {
    var attachment = saveAttachment();
    var userId = findUserId("admin@reportportal.internal");
    var expiresAt = Instant.now().minusSeconds(60).getEpochSecond();
    var signature = createSignature(attachment.getId(), PROJECT_KEY, userId, expiresAt);
    var url = createUrl(attachment.getId(), userId, expiresAt, signature);

    mockMvc.perform(get(url))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturnForbiddenWhenIssuerHasNoProjectAccess() throws Exception {
    var attachment = saveAttachment();
    var userId = createUserWithoutAccess();
    var url = createSignedUrl(attachment.getId(), userId);

    mockMvc.perform(get(url))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldKeepExistingStreamEndpointProtectedAndWorking() throws Exception {
    var attachment = saveAttachment();
    var url = "/v1/data/" + PROJECT_KEY + "/streams/" + attachment.getId();

    mockMvc.perform(get(url))
        .andExpect(status().isUnauthorized());

    var initialResult = mockMvc.perform(
            get(url).with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(initialResult))
        .andExpect(status().isOk())
        .andExpect(content().bytes(FILE_CONTENT));
  }

  @Test
  void shouldUseRangeReadForExistingAuthenticatedStreamEndpoint() throws Exception {
    var attachment = saveAttachment();
    var url = "/v1/data/" + PROJECT_KEY + "/streams/" + attachment.getId();

    var initialResult = mockMvc.perform(get(url)
            .header(HttpHeaders.RANGE, "bytes=0-1023")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(initialResult))
        .andExpect(status().isPartialContent())
        .andExpect(header().string(
            HttpHeaders.CONTENT_RANGE,
            "bytes 0-1023/" + FILE_CONTENT.length
        ))
        .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 1024))
        .andExpect(content().bytes(java.util.Arrays.copyOfRange(FILE_CONTENT, 0, 1024)));
  }

  private ResultActions performStream(String url) throws Exception {
    return performStream(url, null);
  }

  private ResultActions performStream(String url, String range) throws Exception {
    var requestBuilder = get(url);
    if (range != null) {
      requestBuilder.header(HttpHeaders.RANGE, range);
    }

    var initialResult = mockMvc.perform(requestBuilder)
        .andExpect(request().asyncStarted())
        .andReturn();
    return mockMvc.perform(asyncDispatch(initialResult));
  }

  private Attachment saveAttachment() {
    var metaInfo = AttachmentMetaInfo.builder()
        .withProjectId(1L)
        .withCreationDate(Instant.now())
        .withItemId(1L)
        .withLaunchId(1L)
        .withLogId(1L)
        .withLogUuid("stream-test")
        .withLaunchUuid("stream-test")
        .withFileName("video.bin")
        .build();
    var file = new MockMultipartFile(
        "file",
        "video.bin",
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        FILE_CONTENT
    );
    Optional<BinaryDataMetaInfo> binaryDataMetaInfo =
        attachmentBinaryDataService.saveAttachment(metaInfo, file);
    assertTrue(binaryDataMetaInfo.isPresent());
    attachmentBinaryDataService.attachToLog(binaryDataMetaInfo.orElseThrow(), metaInfo);
    return attachmentRepository.findByFileId(binaryDataMetaInfo.orElseThrow().getFileId())
        .orElseThrow();
  }

  private long findUserId(String login) {
    return userRepository.findReportPortalUser(login).orElseThrow().getUserId();
  }

  private long createUserWithoutAccess() {
    var user = new User();
    user.setLogin("stream-no-access@reportportal.internal");
    user.setEmail("stream-no-access@reportportal.internal");
    user.setPassword("password");
    user.setFullName("Stream No Access");
    user.setRole(UserRole.USER);
    user.setUserType(UserType.INTERNAL);
    user.setActive(true);
    return userRepository.saveAndFlush(user).getId();
  }

  private String createSignedUrl(long attachmentId, long userId) {
    var link = fileSignedLinkService.createLink(attachmentId, PROJECT_KEY, userId);
    return createUrl(
        attachmentId,
        userId,
        link.expiresAtEpochSecond(),
        link.signature()
    );
  }

  private String createUrl(long attachmentId, long userId, long expiresAt, String signature) {
    return UriComponentsBuilder.fromPath("/v1/public/data/streams/{dataId}")
        .queryParam("pk", PROJECT_KEY)
        .queryParam("uid", userId)
        .queryParam("exp", expiresAt)
        .queryParam("sig", signature)
        .buildAndExpand(attachmentId)
        .encode()
        .toUriString();
  }

  private String createSignature(
      long attachmentId,
      String projectKey,
      long userId,
      long expiresAt
  ) throws Exception {
    var keyMac = Mac.getInstance("HmacSHA256");
    keyMac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    var streamKey = keyMac.doFinal(
        "rp-attachment-stream-v1".getBytes(StandardCharsets.UTF_8));

    var signatureMac = Mac.getInstance("HmacSHA256");
    signatureMac.init(new SecretKeySpec(streamKey, "HmacSHA256"));
    var canonical = String.join("\n",
        "v1",
        Long.toString(attachmentId),
        projectKey,
        Long.toString(userId),
        Long.toString(expiresAt)
    );
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(signatureMac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
  }

  private static byte[] createFileContent() {
    var content = new byte[2048];
    for (int index = 0; index < content.length; index++) {
      content[index] = (byte) (index % 251);
    }
    return content;
  }
}
