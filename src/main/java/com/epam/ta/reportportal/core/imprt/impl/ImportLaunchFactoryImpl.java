/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
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
package com.epam.ta.reportportal.core.imprt.impl;

import com.epam.ta.reportportal.core.imprt.impl.junit.AsyncJunitImportLaunch;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ImportLaunchFactoryImpl implements ImportLaunchFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final Map<ImportType, Class<? extends ImportLaunch>> MAPPING =
            ImmutableMap.<ImportType, Class<? extends ImportLaunch>>builder()
                    .put(ImportType.JUNIT, AsyncJunitImportLaunch.class)
                    .build();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public ImportLaunch getImportLaunch(ImportType type) {
        return applicationContext.getBean(MAPPING.get(type));
    }
}
