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

import static com.epam.reportportal.base.core.user.impl.CreateUserHandlerImpl.BID_TYPE;

import com.epam.reportportal.base.core.launch.util.LinkGenerator;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserCreationBid;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Pending self-service email changes. A change requested by the owner of an account is stored as a
 * {@link UserCreationBid} of type {@value #EMAIL_CHANGE_BID_TYPE} and applied only when the link mailed to the new
 * address is opened.
 *
 * <p>Storage: {@code uuid} is the single-use token, {@code email} the requested new address, {@code inviting_user_id}
 * the account being changed (the foreign key cascades, so pending changes disappear with the account) and
 * {@code metadata.metadata} holds {@code type}, {@code userId} and {@code currentEmail}. Invitation code looks bids up
 * by {@code type = internal} only, so these rows are never treated as invitations, and invitations are never treated as
 * email changes. Rows older than one day are also removed by {@code CleanExpiredCreationBidsJob}, which effectively caps
 * {@code rp.email.change.ttl} at one day.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailChangeConfirmationService {

  public static final String EMAIL_CHANGE_BID_TYPE = "email_change";
  static final String USER_ID = "userId";
  static final String CURRENT_EMAIL = "currentEmail";

  private final UserCreationBidRepository userCreationBidRepository;
  private final LinkGenerator linkGenerator;
  private final HttpServletRequest httpServletRequest;
  private final MailServiceFactory emailServiceFactory;
  private final ThreadPoolTaskExecutor emailExecutorService;

  @Value("${rp.email.change.ttl:PT24H}")
  private Duration emailChangeTtl;

  /**
   * A confirmed-to-be change of the email of user {@code userId} from {@code currentEmail} to {@code newEmail}.
   */
  public record PendingEmailChange(String uuid, Long userId, String currentEmail, String newEmail) {

  }

  /**
   * Records a pending change of the user's email to {@code newEmail}, replacing any earlier pending change of the same
   * user, and mails a confirmation link to the new address and a notice to the current one. The mails are sent after
   * the surrounding transaction commits, through the email executor; a failure is only logged.
   *
   * @param user     the account whose email is to change
   * @param newEmail the normalized, validated and currently unused new address
   */
  public void requestChange(User user, String newEmail) {
    var baseUrl = linkGenerator.composeTrustedBaseUrl(httpServletRequest);
    if (baseUrl.isEmpty()) {
      log.warn("Email change requested for user id={} from an untrusted host; change not recorded, e-mail not sent",
          user.getId());
      return;
    }

    userCreationBidRepository.deleteByInvitingUserIdAndType(user.getId(), EMAIL_CHANGE_BID_TYPE);

    Map<String, Object> meta = new HashMap<>();
    meta.put(BID_TYPE, EMAIL_CHANGE_BID_TYPE);
    meta.put(USER_ID, user.getId());
    meta.put(CURRENT_EMAIL, user.getEmail());

    var bid = new UserCreationBid();
    bid.setUuid(UUID.randomUUID().toString());
    bid.setEmail(newEmail);
    bid.setInvitingUser(user);
    bid.setMetadata(new Metadata(meta));
    userCreationBidRepository.save(bid);

    var confirmLink = baseUrl.get() + "#login?confirmEmail=" + bid.getUuid();
    var currentEmail = user.getEmail();
    var login = user.getLogin();
    var userId = user.getId();
    afterCommit(() -> sendMails(userId, login, currentEmail, newEmail, confirmLink));
  }

  /**
   * Finds a pending, not expired email change by its token.
   *
   * @param uuid the token from the confirmation link
   * @return the pending change, or empty when the token is unknown, already used or expired
   */
  public Optional<PendingEmailChange> findPending(String uuid) {
    if (uuid == null) {
      return Optional.empty();
    }
    return userCreationBidRepository.findByUuidAndType(uuid, EMAIL_CHANGE_BID_TYPE)
        .filter(bid -> !isExpired(bid))
        .flatMap(EmailChangeConfirmationService::toPendingChange);
  }

  /**
   * Removes all pending email changes of the user, including the one just confirmed.
   *
   * @param userId the user id
   */
  public void completeChange(Long userId) {
    userCreationBidRepository.deleteByInvitingUserIdAndType(userId, EMAIL_CHANGE_BID_TYPE);
  }

  private static Optional<PendingEmailChange> toPendingChange(UserCreationBid bid) {
    Map<String, Object> meta = Optional.ofNullable(bid.getMetadata()).map(Metadata::getMetadata).orElse(Map.of());
    Long userId = toLong(meta.get(USER_ID));
    Object currentEmail = meta.get(CURRENT_EMAIL);
    if (userId == null || currentEmail == null || bid.getEmail() == null
        || bid.getInvitingUser() == null || !userId.equals(bid.getInvitingUser().getId())) {
      log.warn("Malformed email change bid {}; ignored", bid.getUuid());
      return Optional.empty();
    }
    return Optional.of(new PendingEmailChange(bid.getUuid(), userId, currentEmail.toString(), bid.getEmail()));
  }

  private static Long toLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value instanceof String string) {
      try {
        return Long.parseLong(string);
      } catch (NumberFormatException _) {
        return null;
      }
    }
    return null;
  }

  private boolean isExpired(UserCreationBid bid) {
    Instant issuedAt = bid.getLastModified();
    return issuedAt == null || issuedAt.plus(emailChangeTtl).isBefore(Instant.now());
  }

  private void sendMails(Long userId, String login, String currentEmail, String newEmail, String confirmLink) {
    try {
      emailExecutorService.execute(() -> {
        try {
          var emailService = emailServiceFactory.getDefaultEmailService(true);
          emailService.sendEmailChangeConfirmation("Confirm your new email address",
              new String[]{newEmail}, confirmLink, login);
          emailService.sendEmailChangeRequestedNotice("Email change requested",
              new String[]{currentEmail}, newEmail, login);
        } catch (Exception e) {
          log.warn("Unable to send email change e-mails for user id={}", userId, e);
        }
      });
    } catch (TaskRejectedException e) {
      log.warn("Email change e-mails for user id={} were not queued: {}", userId, e.getMessage());
    }
  }

  /**
   * Runs the action once the current transaction commits, so a request that fails later (and rolls back the pending
   * change) does not mail a link that no longer works; runs it at once outside a transaction.
   */
  private static void afterCommit(Runnable action) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          action.run();
        }
      });
    } else {
      action.run();
    }
  }
}
