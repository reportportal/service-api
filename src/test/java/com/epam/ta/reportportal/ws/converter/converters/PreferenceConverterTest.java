package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel_Bortnik
 */
public class PreferenceConverterTest {

    @Test
    public void testConvert(){
        UserPreference userPreference = new UserPreference();
        userPreference.setId("id");
        UserPreference.LaunchTabs tabs = new UserPreference.LaunchTabs();
        tabs.setActive("active");
        tabs.setFilters(ImmutableList.<String>builder().add("filter").build());
        userPreference.setLaunchTabs(tabs);
        userPreference.setProjectRef("project");
        userPreference.setUserRef("user");
        PreferenceResource resource = PreferenceConverter.TO_RESOURCE.apply(userPreference);

        Assert.assertEquals(userPreference.getProjectRef(), resource.getProjectRef());
        Assert.assertEquals(userPreference.getLaunchTabs().getFilters(), resource.getFilters());
        Assert.assertEquals(userPreference.getLaunchTabs().getActive(), resource.getActive());
        Assert.assertEquals(userPreference.getUserRef(), resource.getUserRef());
    }

}