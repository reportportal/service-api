/*
 * Copyright 2016 EPAM Systems
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
package com.epam.ta.reportportal.auth.endpoint;

import com.epam.ta.reportportal.store.jooq.enums.JProjectRoleEnum;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Shows list of supported user roles
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class MetadataContributor implements InfoContributor {

	@Override
	public void contribute(Info.Builder builder) {
		//@formatter:off
        builder
                .withDetail("metadata", ImmutableMap
                        .builder()
                        .put("project_roles",
                                stream(JProjectRoleEnum.values()).map(Enum::name).collect(Collectors.toList()))
                        .build());
        //@formatter:on
	}

}
