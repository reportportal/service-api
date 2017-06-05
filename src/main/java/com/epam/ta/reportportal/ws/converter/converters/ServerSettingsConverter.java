package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.settings.ServerEmailDetails;
import com.epam.ta.reportportal.database.entity.settings.ServerSettings;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import org.apache.commons.lang3.BooleanUtils;

import java.util.function.Function;

public final class ServerSettingsConverter {

    private ServerSettingsConverter() {
        //static only
    }

    public static final Function<ServerSettings, ServerSettingsResource> TO_RESOURCE = settings -> {
        ServerSettingsResource resource = new ServerSettingsResource();
        resource.setProfile(settings.getId());
        resource.setActive(settings.getActive());
        ServerEmailDetails serverEmailDetails = settings.getServerEmailDetails();
        if (null != serverEmailDetails) {
            ServerEmailResource output = new ServerEmailResource();
            output.setHost(serverEmailDetails.getHost());
            output.setPort(serverEmailDetails.getPort());
            output.setProtocol(serverEmailDetails.getProtocol());
            output.setAuthEnabled(serverEmailDetails.getAuthEnabled());
            output.setSslEnabled(BooleanUtils.isTrue(serverEmailDetails.getSslEnabled()) );
            output.setStarTlsEnabled(BooleanUtils.isTrue(serverEmailDetails.getStarTlsEnabled()));
            output.setFrom(serverEmailDetails.getFrom());
            if (serverEmailDetails.getAuthEnabled()) {
                output.setUsername(serverEmailDetails.getUsername());
				/* Password field not provided in response */
            }
            resource.setServerEmailResource(output);
        }
        return resource;
    };

}
