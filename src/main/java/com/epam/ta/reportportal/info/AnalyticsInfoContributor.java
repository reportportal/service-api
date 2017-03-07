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
package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.entity.settings.AnalyticsDetails;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Shows list of supported analytics providers.
 *
 * @author Pavel Bortnik
 */

@Component
public class AnalyticsInfoContributor implements ExtensionContributor {

    private static final String ANALYTICS_KEY = "analytics";

    private final ServerSettingsRepository settingsRepository;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public AnalyticsInfoContributor(ServerSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    public Map<String, Object> contribute() {
        Optional<Map<String, AnalyticsDetails>> analytics = Optional.ofNullable(settingsRepository.findOne("default"))
                .flatMap(settings -> Optional.ofNullable(settings.getAnalyticsDetails()));
        return analytics.isPresent()
                ? ImmutableMap.<String, Object>builder().put(ANALYTICS_KEY, analytics.get()).build()
                : Collections.emptyMap();
    }
}
