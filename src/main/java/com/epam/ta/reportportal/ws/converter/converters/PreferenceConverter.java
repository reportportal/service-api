package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;

import java.util.Optional;
import java.util.function.Function;

public final class PreferenceConverter {

    private PreferenceConverter() {
        //static only
    }

    public static final Function<UserPreference, PreferenceResource> TO_RESOURCE = preference -> {
        PreferenceResource preferenceResource = new PreferenceResource();
        preferenceResource.setUserRef(preference.getUserRef());
        preferenceResource.setProjectRef(preference.getProjectRef());
        UserPreference.LaunchTabs tabs = preference.getLaunchTabs();
        if (Optional.ofNullable(tabs).isPresent()) {
            preferenceResource.setActive(tabs.getActive());
            preferenceResource.setFilters(tabs.getFilters());
        }
        return preferenceResource;
    };

}
