/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.auth.store;

/**
 * AuthConfig repository custom
 *
 * @author Andrei Varabyeu
 */
//public class AuthConfigRepositoryImpl implements AuthConfigRepositoryCustom {
public class AuthConfigRepositoryImpl {

//    private final DSLContext dslContext;
//
//    @Autowired
//    public AuthConfigRepositoryImpl(DSLContext dslContext) {
//        this.dslContext = dslContext;
//        createDefaultProfileIfAbsent();
//    }
//
//    @Override
//    public void createDefaultProfileIfAbsent() {
//        if (null == mongoOperations.findOne(findDefaultQuery(), AuthConfigEntity.class)) {
//            AuthConfigEntity entity = new AuthConfigEntity();
//            entity.setId(AuthConfigRepository.DEFAULT_PROFILE);
//            mongoOperations.save(entity);
//        }
//    }
//
//    @Override
//    public void deleteSettings(AuthIntegrationType type) {
//        mongoOperations.updateFirst(findDefaultQuery(), new Update().unset(type.getDbField()), AuthConfigEntity.class);
//    }
//
//    @Override
//    public void updatePartially(AuthConfigEntity entity) {
//        mongoOperations.updateFirst(findDefaultQuery(), updateExisting(entity), AuthConfigEntity.class);
//    }
//
//    @Override
//    public void updateLdap(LdapConfig ldapConfig) {
//        mongoOperations
//                .updateFirst(findDefaultQuery(), Update.update(AuthIntegrationType.LDAP.getDbField(), ldapConfig),
//                        AuthConfigEntity.class);
//
//    }
//
//    @Override
//    public void updateActiveDirectory(ActiveDirectoryConfig adConfig) {
//        mongoOperations
//                .updateFirst(findDefaultQuery(),
//                        Update.update(AuthIntegrationType.ACTIVE_DIRECTORY.getDbField(), adConfig),
//                        AuthConfigEntity.class);
//    }
//
//    @Override
//    public Optional<LdapConfig> findLdap(boolean enabled) {
//        return ofNullable(
//                mongoOperations.findOne(findDefaultQuery().addCriteria(Criteria.where("ldap.enabled").is(enabled)),
//                        AuthConfigEntity.class))
//                .flatMap(cfg -> ofNullable(cfg.getLdap()));
//    }
//
//    @Override
//    public Optional<ActiveDirectoryConfig> findActiveDirectory(boolean enabled) {
//        return ofNullable(mongoOperations
//                .findOne(findDefaultQuery().addCriteria(Criteria.where("activeDirectory.enabled").is(enabled)),
//                        AuthConfigEntity.class))
//                .flatMap(cfg -> ofNullable(cfg.getActiveDirectory()));
//    }
//
//    private Query findDefaultQuery() {
//        return query(where("_id").is(AuthConfigRepository.DEFAULT_PROFILE));
//    }
//
//    private Update updateExisting(Object object) {
//        try {
//            Update update = new Update();
//            PropertyUtils.describe(object).entrySet().stream().filter(e -> null != e.getValue())
//                    .forEach(it -> update.set(it.getKey(), it.getValue()));
//            return update;
//        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//            throw new ReportPortalException("Error during auth config update", e);
//        }
//    }

}
