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

package com.epam.reportportal.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserCreationBid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/fill/user-bid/user-bid-fill.sql")
class UserCreationBidRepositoryTest extends BaseMvcTest {

  public static final String INTERNAL_TYPE = "internal";
  public static final String UNKNOWN_TYPE = "unknown";

  @Autowired
  private UserCreationBidRepository repository;

  @Test
  void findByUuidAndType() {
    final String adminUuid = "0647cf8f-02e3-4acd-ba3e-f74ec9d2c5cb";

    final Optional<UserCreationBid> userBid = repository.findByUuidAndType(adminUuid,
        INTERNAL_TYPE);

    assertTrue(userBid.isPresent(), "User bid should exists");
    assertEquals(adminUuid, userBid.get().getUuid(), "Incorrect uuid");
    assertEquals("superadminemail@domain.com", userBid.get().getEmail(), "Incorrect email");
  }

	@Test
	void shouldNotFindByUuidAndTypeWhenTypeNotMatched() {
		final String adminUuid = "0647cf8f-02e3-4acd-ba3e-f74ec9d2c5cb";

		final Optional<UserCreationBid> userBid = repository.findByUuidAndType(adminUuid, UNKNOWN_TYPE);

		assertTrue(userBid.isEmpty(), "User bid should not exists");
	}

  @Test
  void expireBidsOlderThan() {
    Instant date = LocalDateTime.now().minusDays(20).atZone(ZoneId.systemDefault()).toInstant();

    int deletedCount = repository.expireBidsOlderThan(date);
    final List<UserCreationBid> bids = repository.findAll();

    assertEquals(1, deletedCount);
    bids.forEach(it -> assertTrue(it.getLastModified().isAfter(date), "Incorrect date"));
  }

  @Test
  void findById() {
    final String adminUuid = "0647cf8f-02e3-4acd-ba3e-f74ec9d2c5cb";

    final Optional<UserCreationBid> bid = repository.findById(adminUuid);

    assertTrue(bid.isPresent(), "User bid should exists");
    assertEquals(adminUuid, bid.get().getUuid(), "Incorrect uuid");
    assertEquals("superadminemail@domain.com", bid.get().getEmail(), "Incorrect email");
  }

  @Test
  void deleteAllByEmail() {
    int deletedCount = repository.deleteAllByEmail("defaultemail@domain.com");
    assertEquals(2, deletedCount);
  }
}
