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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserCreationBid;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repository for user creation bid rows.
 *
 * @author Ivan Budaev
 */
public interface UserCreationBidRepository extends ReportPortalRepository<UserCreationBid, String>,
    UserCreationBidRepositoryCustom {

  @Query(value = "SELECT bid.* FROM user_creation_bid bid WHERE bid.uuid = :uuid AND (bid.metadata -> 'metadata'->>'type' = :type)", nativeQuery = true)
  Optional<UserCreationBid> findByUuidAndType(@Param("uuid") String uuid,
      @Param("type") String type);

  @Modifying
  @Query(value = "DELETE FROM UserCreationBid u WHERE  u.lastModified < :date")
  int expireBidsOlderThan(@Param("date") Instant date);

  /**
   * Deletes the bids of the given type that reference the given user through {@code inviting_user_id}.
   *
   * @param userId the user id
   * @param type   the bid type stored in {@code metadata.metadata.type}
   * @return number of deleted rows
   */
  @Modifying
  @Query(value = "DELETE FROM user_creation_bid bid WHERE bid.inviting_user_id = :userId AND (bid.metadata -> 'metadata'->>'type' = :type)", nativeQuery = true)
  int deleteByInvitingUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

  /**
   * Atomically increments the failed-activation counter kept in {@code metadata.metadata.activationFailures} of the
   * bid. Native on purpose: it does not touch {@code last_modified} (which the JPA auditing listener would bump,
   * extending the bid's lifetime) and concurrent increments serialize on the row lock. The persistence context is
   * cleared so a stale copy of the bid is never flushed over the new value.
   *
   * @param uuid the bid uuid
   * @param type the bid type stored in {@code metadata.metadata.type}
   * @return number of updated rows
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(value = "UPDATE user_creation_bid SET metadata = jsonb_set(metadata, '{metadata,activationFailures}',"
      + " to_jsonb(COALESCE((metadata -> 'metadata' ->> 'activationFailures')::int, 0) + 1))"
      + " WHERE uuid = :uuid AND (metadata -> 'metadata' ->> 'type' = :type)", nativeQuery = true)
  int incrementActivationFailures(@Param("uuid") String uuid, @Param("type") String type);

  /**
   * Deletes the bid when its failed-activation counter reached the limit.
   *
   * @param uuid  the bid uuid
   * @param type  the bid type stored in {@code metadata.metadata.type}
   * @param limit the number of failures that invalidates the bid
   * @return number of deleted rows
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(value = "DELETE FROM user_creation_bid WHERE uuid = :uuid AND (metadata -> 'metadata' ->> 'type' = :type)"
      + " AND COALESCE((metadata -> 'metadata' ->> 'activationFailures')::int, 0) >= :limit", nativeQuery = true)
  int deleteIfActivationFailuresReached(@Param("uuid") String uuid, @Param("type") String type,
      @Param("limit") int limit);

  /**
   * Finds the newest bid of the given type that references the given user through {@code inviting_user_id}.
   *
   * @param userId the user id
   * @param type   the bid type stored in {@code metadata.metadata.type}
   * @return the newest such bid, if any
   */
  @Query(value = "SELECT bid.* FROM user_creation_bid bid WHERE bid.inviting_user_id = :userId AND (bid.metadata -> 'metadata'->>'type' = :type) ORDER BY bid.last_modified DESC LIMIT 1", nativeQuery = true)
  Optional<UserCreationBid> findLatestByInvitingUserIdAndType(@Param("userId") Long userId,
      @Param("type") String type);

  Optional<UserCreationBid> findFirstByEmailOrderByLastModifiedDesc(String email);

  void deleteByUuid(String uuid);
}
