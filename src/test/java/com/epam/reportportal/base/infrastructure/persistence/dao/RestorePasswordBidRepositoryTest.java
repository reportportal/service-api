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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.user.RestorePasswordBid;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class RestorePasswordBidRepositoryTest extends BaseMvcTest {

  @Autowired
  private RestorePasswordBidRepository restorePasswordBidRepository;

  @Test
  void findByEmail() {
    Optional<RestorePasswordBid> bid = restorePasswordBidRepository.findByEmail(
        "notexisted@email.com");
    assertFalse(bid.isPresent());
  }

  @Test
  void findByExistedEmail() {
    RestorePasswordBid restorePasswordBid = new RestorePasswordBid();
    restorePasswordBid.setUuid("uuid");
    restorePasswordBid.setEmail("existed@email.com");
    restorePasswordBid.setLastModifiedDate(Instant.now());
    restorePasswordBidRepository.save(restorePasswordBid);
    Optional<RestorePasswordBid> bid = restorePasswordBidRepository.findByEmail(
        "existed@email.com");
    assertTrue(bid.isPresent());
  }
}
