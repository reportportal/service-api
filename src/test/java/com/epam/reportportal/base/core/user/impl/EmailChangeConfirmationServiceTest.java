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

package com.epam.reportportal.base.core.user.impl;

import static com.epam.reportportal.base.core.user.impl.EmailChangeConfirmationService.EMAIL_CHANGE_BID_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.launch.util.LinkGenerator;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserCreationBid;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.email.EmailService;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class EmailChangeConfirmationServiceTest {

  private static final String BASE_URL = "https://rp.example.com";

  @Mock
  private UserCreationBidRepository userCreationBidRepository;

  @Mock
  private LinkGenerator linkGenerator;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private MailServiceFactory emailServiceFactory;

  @Mock
  private ThreadPoolTaskExecutor emailExecutorService;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private EmailChangeConfirmationService service;

  private User user;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "emailChangeTtl", Duration.ofHours(24));
    ReflectionTestUtils.setField(service, "emailChangeCooldown", Duration.ofSeconds(60));
    user = new User();
    user.setId(10L);
    user.setLogin("user@example.com");
    user.setEmail("user@example.com");
  }

  @AfterEach
  void tearDown() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  private void runExecutorInline() {
    doAnswer(inv -> {
      ((Runnable) inv.getArgument(0)).run();
      return null;
    }).when(emailExecutorService).execute(any(Runnable.class));
  }

  @Test
  void requestChangeShouldReplacePreviousPendingChangeAndStoreTypedBid() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));

    service.requestChange(user, "new@example.com");

    var inOrder = inOrder(userCreationBidRepository);
    inOrder.verify(userCreationBidRepository).deleteByInvitingUserIdAndType(10L, EMAIL_CHANGE_BID_TYPE);
    ArgumentCaptor<UserCreationBid> captor = ArgumentCaptor.forClass(UserCreationBid.class);
    inOrder.verify(userCreationBidRepository).save(captor.capture());

    UserCreationBid bid = captor.getValue();
    assertThat(UUID.fromString(bid.getUuid())).isNotNull();
    assertThat(bid.getEmail()).isEqualTo("new@example.com");
    assertThat(bid.getInvitingUser()).isSameAs(user);
    assertThat(bid.getMetadata().getMetadata())
        .containsEntry("type", EMAIL_CHANGE_BID_TYPE)
        .containsEntry("userId", 10L)
        .containsEntry("currentEmail", "user@example.com")
        .doesNotContainKey("organizations");
  }

  @Test
  void requestChangeShouldMailConfirmationLinkToNewAddressAndNoticeToCurrentOne() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));
    when(emailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
    runExecutorInline();

    service.requestChange(user, "new@example.com");

    ArgumentCaptor<UserCreationBid> captor = ArgumentCaptor.forClass(UserCreationBid.class);
    verify(userCreationBidRepository).save(captor.capture());
    String uuid = captor.getValue().getUuid();

    verify(emailService).sendEmailChangeConfirmation(eq("Confirm your new email address"),
        eq(new String[]{"new@example.com"}), eq(BASE_URL + "#login?confirmEmail=" + uuid), eq("user@example.com"));
    verify(emailService).sendEmailChangeRequestedNotice(eq("Email change requested"),
        eq(new String[]{"user@example.com"}), eq("new@example.com"), eq("user@example.com"));
  }

  @Test
  void requestChangeShouldSendMailsOnlyAfterCommit() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));
    TransactionSynchronizationManager.initSynchronization();

    service.requestChange(user, "new@example.com");

    verifyNoInteractions(emailExecutorService);
    TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
    verify(emailExecutorService).execute(any(Runnable.class));
  }

  @Test
  void requestChangeFromUntrustedHostShouldFailAndRecordNothing() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.requestChange(user, "new@example.com"))
        .isInstanceOf(ReportPortalException.class)
        .hasMessageContaining("confirmation link cannot be generated for this host")
        .hasMessageContaining("administrator");

    verifyNoInteractions(userCreationBidRepository, emailExecutorService, emailServiceFactory);
  }

  @Test
  void requestChangeForSameAddressWithinCooldownShouldKeepPendingChangeAndSendNothing() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));
    when(userCreationBidRepository.findLatestByInvitingUserIdAndType(10L, EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.of(emailChangeBid(Instant.now().minusSeconds(10))));

    service.requestChange(user, "new@example.com");

    verify(userCreationBidRepository, never()).deleteByInvitingUserIdAndType(any(), anyString());
    verify(userCreationBidRepository, never()).save(any());
    verifyNoInteractions(emailExecutorService, emailServiceFactory);
  }

  @Test
  void requestChangeForDifferentAddressWithinCooldownShouldBeRejected() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));
    when(userCreationBidRepository.findLatestByInvitingUserIdAndType(10L, EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.of(emailChangeBid(Instant.now().minusSeconds(10))));

    assertThatThrownBy(() -> service.requestChange(user, "typo-fixed@example.com"))
        .isInstanceOf(ReportPortalException.class)
        .hasMessageContaining("Try again in about 60 seconds");

    verify(userCreationBidRepository, never()).deleteByInvitingUserIdAndType(any(), anyString());
    verify(userCreationBidRepository, never()).save(any());
    verifyNoInteractions(emailExecutorService, emailServiceFactory);
  }

  @Test
  void requestChangeAfterCooldownShouldReplacePendingChangeAndSendMails() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));
    when(userCreationBidRepository.findLatestByInvitingUserIdAndType(10L, EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.of(emailChangeBid(Instant.now().minusSeconds(61))));

    service.requestChange(user, "other@example.com");

    verify(userCreationBidRepository).deleteByInvitingUserIdAndType(10L, EMAIL_CHANGE_BID_TYPE);
    verify(userCreationBidRepository).save(any(UserCreationBid.class));
    verify(emailExecutorService).execute(any(Runnable.class));
  }

  @Test
  void mailFailureShouldOnlyBeLogged() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));
    when(emailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
    doThrow(new RuntimeException("smtp down")).when(emailService)
        .sendEmailChangeConfirmation(anyString(), any(), anyString(), anyString());
    runExecutorInline();

    service.requestChange(user, "new@example.com");

    verify(userCreationBidRepository).save(any(UserCreationBid.class));
  }

  @Test
  void rejectedMailTaskShouldOnlyBeLogged() {
    when(linkGenerator.composeTrustedBaseUrl(httpServletRequest)).thenReturn(Optional.of(BASE_URL));
    doThrow(new TaskRejectedException("queue full")).when(emailExecutorService).execute(any(Runnable.class));

    service.requestChange(user, "new@example.com");

    verify(userCreationBidRepository).save(any(UserCreationBid.class));
  }

  @Test
  void findPendingShouldReturnFreshEmailChangeBid() {
    var bid = emailChangeBid(Instant.now());
    when(userCreationBidRepository.findByUuidAndType(bid.getUuid(), EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.of(bid));

    var pending = service.findPending(bid.getUuid());

    assertThat(pending).isPresent();
    assertThat(pending.get().userId()).isEqualTo(10L);
    assertThat(pending.get().currentEmail()).isEqualTo("user@example.com");
    assertThat(pending.get().newEmail()).isEqualTo("new@example.com");
  }

  @Test
  void findPendingShouldIgnoreExpiredBid() {
    var bid = emailChangeBid(Instant.now().minus(25, ChronoUnit.HOURS));
    when(userCreationBidRepository.findByUuidAndType(bid.getUuid(), EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.of(bid));

    assertThat(service.findPending(bid.getUuid())).isEmpty();
  }

  @Test
  void findPendingShouldHonourConfiguredTtl() {
    ReflectionTestUtils.setField(service, "emailChangeTtl", Duration.ofMinutes(30));
    var bid = emailChangeBid(Instant.now().minus(31, ChronoUnit.MINUTES));
    when(userCreationBidRepository.findByUuidAndType(bid.getUuid(), EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.of(bid));

    assertThat(service.findPending(bid.getUuid())).isEmpty();
  }

  @Test
  void findPendingShouldLookUpOnlyEmailChangeBids() {
    when(userCreationBidRepository.findByUuidAndType("invitation-uuid", EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.empty());

    assertThat(service.findPending("invitation-uuid")).isEmpty();
    verify(userCreationBidRepository, never()).findById(any());
  }

  @Test
  void findPendingShouldIgnoreBidWhoseUserDoesNotMatchMetadata() {
    var bid = emailChangeBid(Instant.now());
    var other = new User();
    other.setId(11L);
    bid.setInvitingUser(other);
    when(userCreationBidRepository.findByUuidAndType(bid.getUuid(), EMAIL_CHANGE_BID_TYPE))
        .thenReturn(Optional.of(bid));

    assertThat(service.findPending(bid.getUuid())).isEmpty();
  }

  @Test
  void findPendingWithNullTokenShouldBeEmpty() {
    assertThat(service.findPending(null)).isEmpty();
    verifyNoInteractions(userCreationBidRepository);
  }

  @Test
  void completeChangeShouldDeleteAllPendingChangesOfUser() {
    service.completeChange(10L);

    verify(userCreationBidRepository).deleteByInvitingUserIdAndType(10L, EMAIL_CHANGE_BID_TYPE);
  }

  private UserCreationBid emailChangeBid(Instant lastModified) {
    Map<String, Object> meta = new HashMap<>();
    meta.put("type", EMAIL_CHANGE_BID_TYPE);
    // JSON numbers come back from jsonb as Integer
    meta.put("userId", 10);
    meta.put("currentEmail", "user@example.com");
    var bid = new UserCreationBid();
    bid.setUuid(UUID.randomUUID().toString());
    bid.setEmail("new@example.com");
    bid.setInvitingUser(user);
    bid.setLastModified(lastModified);
    bid.setMetadata(new Metadata(meta));
    return bid;
  }
}
