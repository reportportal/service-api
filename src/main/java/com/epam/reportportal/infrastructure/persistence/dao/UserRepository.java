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

import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserAuthProjection;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserIdDisplayNameProjection;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Ivan Budayeu
 */
public interface UserRepository extends ReportPortalRepository<User, Long>, UserRepositoryCustom {

  @Query(value = "SELECT id FROM users WHERE users.login = :username FOR UPDATE", nativeQuery = true)
  Optional<Long> findIdByLoginForUpdate(@Param("username") String login);

  Optional<User> findByEmail(String email);

  /**
   * @param login user login for search
   * @return {@link Optional} of {@link User}
   */
  Optional<User> findByLogin(String login);

  /**
   * @param uuid user uuid for search
   * @return {@link Optional} of {@link User}
   */
  Optional<User> findByUuid(UUID uuid);

  /**
   * @param externalId user external id for search
   * @return {@link Optional} of {@link User}
   */
  Optional<User> findByExternalId(String externalId);

  List<User> findAllByEmailIn(Collection<String> mails);

  List<User> findAllByLoginIn(Set<String> loginSet);

  List<User> findAllByRole(UserRole role);

  @Query(value = "SELECT u FROM User u WHERE u.userType = :userType AND u.isExpired = :isExpired")
  Page<User> findAllByUserTypeAndExpired(@Param("userType") UserType userType,
      @Param("isExpired") boolean isExpired, Pageable pageable);

  @Modifying(clearAutomatically = true)
  @Query(value = "UPDATE users SET expired = TRUE WHERE CAST(metadata-> 'metadata' ->> 'last_login' AS DOUBLE PRECISION) < (extract(EPOCH FROM CAST (:lastLogin AS TIMESTAMP)) * 1000);", nativeQuery = true)
  void expireUsersLoggedOlderThan(@Param("lastLogin") Instant lastLogin);

  /**
   * Updates user's last login value
   *
   * @param username User
   */
  @Modifying(clearAutomatically = true)
  @Query(value = "UPDATE users SET metadata = jsonb_set(metadata, '{metadata,last_login}', to_jsonb(round(extract(EPOCH from clock_timestamp()) * 1000)), TRUE ) WHERE login = :username", nativeQuery = true)
  void updateLastLoginDate(@Param("username") String username);

  @Query(value = "SELECT u.login FROM users u JOIN project_user pu ON u.id = pu.user_id WHERE pu.project_id = :projectId", nativeQuery = true)
  List<String> findNamesByProject(@Param("projectId") Long projectId);

  @Query(value = "SELECT u.email FROM users u JOIN project_user pu ON u.id = pu.user_id WHERE pu.project_id = :projectId", nativeQuery = true)
  List<String> findEmailsByProject(@Param("projectId") Long projectId);

  @Query(value = "SELECT u.email FROM users u JOIN organization_user ou ON u.id = ou.user_id WHERE ou.organization_id = :organizationId", nativeQuery = true)
  List<String> findEmailsByOrganization(@Param("organizationId") Long organizationId);

  @Query(value = """
      SELECT u.email FROM users u
            JOIN organization_user ou ON u.id = ou.user_id
            WHERE ou.organization_id = :organizationId 
            AND ou.organization_role = cast(:#{#organizationRole.name()} AS organization_role_enum)
      """, nativeQuery = true)
  List<String> findEmailsByOrganizationAndRole(@Param("organizationId") Long organizationId,
      @Param("organizationRole") OrganizationRole organizationRole);

  @Query(value = "SELECT u.email FROM users u JOIN project_user pu ON u.id = pu.user_id WHERE pu.project_id = :projectId AND pu.project_role = cast(:#{#projectRole.name()} AS PROJECT_ROLE_ENUM)", nativeQuery = true)
  List<String> findEmailsByProjectAndRole(@Param("projectId") Long projectId,
      @Param("projectRole") ProjectRole projectRole);

  @Query(value = "SELECT u.login FROM users u JOIN project_user pu ON u.id = pu.user_id WHERE pu.project_id = :projectId AND u.login LIKE %:term%", nativeQuery = true)
  List<String> findNamesByProject(@Param("projectId") Long projectId, @Param("term") String term);

  @Query(value = "SELECT users.login FROM users WHERE users.id = :id", nativeQuery = true)
  Optional<String> findLoginById(@Param("id") Long id);

  /**
   * Batch fetch user display names by user IDs. Display name is the user's full name if available, otherwise the
   * login.
   *
   * @param userIds collection of user IDs
   * @return List of user ID and display name projections
   */
  @Query("SELECT new com.epam.reportportal.infrastructure.persistence.entity.user.UserIdDisplayNameProjection(u.id, COALESCE(u.fullName, u.login)) FROM User u WHERE u.id IN :userIds")
  List<UserIdDisplayNameProjection> findDisplayNamesByIds(@Param("userIds") List<Long> userIds);

  /**
   * Optimized method to find user authentication data by login. Returns only fields needed for authentication.
   *
   * @param login user login for search
   * @return {@link Optional} of {@link UserAuthProjection}
   */
  @Query(value = "SELECT u.id, u.uuid, u.external_id, u.login, u.password, u.email, u.role, u.active, u.expired FROM users u WHERE u.login = :login", nativeQuery = true)
  @Cacheable(
      value = "userAuthDataCache",
      key = "'login_' + #login",
      cacheManager = "caffeineCacheManager"
  )
  Optional<UserAuthProjection> findAuthDataByLogin(@Param("login") String login);

  /**
   * Optimized method to find user authentication data by external ID. Returns only fields needed for authentication.
   *
   * @param externalId user external id for search
   * @return {@link Optional} of {@link UserAuthProjection}
   */
  @Query(value = "SELECT u.id, u.uuid, u.external_id, u.login, u.password, u.email, u.role, u.active, u.expired FROM users u WHERE u.external_id = :externalId", nativeQuery = true)
  @Cacheable(
      value = "userAuthDataCache",
      key = "'externalId_' + #externalId",
      cacheManager = "caffeineCacheManager"
  )
  Optional<UserAuthProjection> findAuthDataByExternalId(@Param("externalId") String externalId);

  /**
   * Saves user entity and evicts cache entries for the user. Evicts both login and externalId cache entries to handle
   * potential changes.
   *
   * @param user User entity to save
   * @return Saved user entity
   */
  @Override
  @Caching(evict = {
      @CacheEvict(
          value = "userAuthDataCache",
          key = "'login_' + #user.login",
          cacheManager = "caffeineCacheManager"
      ),
      @CacheEvict(
          value = "userAuthDataCache",
          key = "'externalId_' + #user.externalId",
          cacheManager = "caffeineCacheManager",
          condition = "#user.externalId != null"
      )
  })
  <S extends User> S save(S user);

  /**
   * Deletes user entity and evicts cache entries for the user. Evicts both login and externalId cache entries.
   *
   * @param user User entity to delete
   */
  @Override
  @Caching(evict = {
      @CacheEvict(
          value = "userAuthDataCache",
          key = "'login_' + #user.login",
          cacheManager = "caffeineCacheManager"
      ),
      @CacheEvict(
          value = "userAuthDataCache",
          key = "'externalId_' + #user.externalId",
          cacheManager = "caffeineCacheManager",
          condition = "#user.externalId != null"
      )
  })
  void delete(User user);

  /**
   * Deletes user entity by ID and evicts all cache entries.
   *
   * @param id User ID
   */
  @Override
  @CacheEvict(
      value = "userAuthDataCache",
      allEntries = true,
      cacheManager = "caffeineCacheManager"
  )
  void deleteById(Long id);

  /**
   * Saves all user entities and evicts all cache entries.
   *
   * @param entities User entities to save
   * @return Saved user entities
   */
  @Override
  @CacheEvict(
      value = "userAuthDataCache",
      allEntries = true,
      cacheManager = "caffeineCacheManager"
  )
  <S extends User> List<S> saveAll(Iterable<S> entities);

  /**
   * Deletes all user entities and evicts all cache entries.
   *
   * @param entities User entities to delete
   */
  @Override
  @CacheEvict(
      value = "userAuthDataCache",
      allEntries = true,
      cacheManager = "caffeineCacheManager"
  )
  void deleteAll(Iterable<? extends User> entities);

  /**
   * Deletes all user entities and evicts all cache entries.
   */
  @Override
  @CacheEvict(
      value = "userAuthDataCache",
      allEntries = true,
      cacheManager = "caffeineCacheManager"
  )
  void deleteAll();

  /**
   * Deletes all users by IDs and evicts all cache entries.
   *
   * @param ids User IDs to delete
   */
  @Override
  @CacheEvict(
      value = "userAuthDataCache",
      allEntries = true,
      cacheManager = "caffeineCacheManager"
  )
  void deleteAllById(Iterable<? extends Long> ids);

}
