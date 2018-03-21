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
package com.epam.ta.reportportal.auth.integration;

import com.epam.ta.reportportal.auth.store.entity.AbstractAuthConfig;
import com.epam.ta.reportportal.auth.store.entity.AuthConfigEntity;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author Andrei Varabyeu
 */
public enum AuthIntegrationType {

    ACTIVE_DIRECTORY("ad", "activeDirectory") {
        @Override
        public Optional<AbstractAuthConfig> get(AuthConfigEntity entity) {
            return ofNullable(entity).map(AuthConfigEntity::getActiveDirectory);
        }
    },
    LDAP("ldap", "ldap") {
        @Override
        public Optional<AbstractAuthConfig> get(AuthConfigEntity entity) {
            return ofNullable(entity).map(AuthConfigEntity::getLdap);
        }
    };

    private String id;
    private String dbField;

    AuthIntegrationType(String id, String dbField) {
        this.id = id;
        this.dbField = dbField;
    }

    public abstract Optional<AbstractAuthConfig> get(AuthConfigEntity entity);

    public String getId() {
        return id;
    }

    public String getDbField() {
        return dbField;
    }

    public static Optional<AuthIntegrationType> fromId(String id) {
        return Arrays.stream(values()).filter(it -> it.id.equalsIgnoreCase(id)).findAny();
    }

    @Override
    public String toString() {
        return this.id;
    }
}
