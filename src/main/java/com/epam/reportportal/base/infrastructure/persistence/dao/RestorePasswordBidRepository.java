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

import com.epam.reportportal.base.infrastructure.persistence.entity.user.RestorePasswordBid;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Token rows for password reset e-mails.
 *
 * @author Ivan Budaev
 */
public interface RestorePasswordBidRepository extends
    ReportPortalRepository<RestorePasswordBid, String> {

  /**
   * Finds a restore password bid by email address.
   *
   * @param email normalized email address to search for
   * @return Optional containing the bid if found, empty otherwise
   */
  Optional<RestorePasswordBid> findByEmail(String email);

  /**
   * Deletes bid by specified email.
   *
   * @param email email
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from RestorePasswordBid b where b.email = :email")
  void deleteByEmail(@Param("email") String email);
}
