/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.database.entity.settings.ServerSettings;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import org.springframework.stereotype.Service;


/**
 * REST Maturity Lvl3 rel object creation for response
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class ServerSettingsResourceAssembler extends ResourceAssembler<ServerSettings, ServerSettingsResource> {

    @Override
    public ServerSettingsResource toResource(ServerSettings serverSettings) {
        ServerSettingsResource resource = new ServerSettingsResource();
        resource.setProfile(serverSettings.getId());
        resource.setActive(serverSettings.getActive());
        if (null != serverSettings.getServerEmailDetails()) {
            ServerEmailResource output = new ServerEmailResource();
            output.setHost(serverSettings.getServerEmailDetails().getHost());
            output.setPort(serverSettings.getServerEmailDetails().getPort());
            output.setProtocol(serverSettings.getServerEmailDetails().getProtocol());
            output.setAuthEnabled(serverSettings.getServerEmailDetails().getAuthEnabled());
            output.setSslEnabled(serverSettings.getServerEmailDetails().isSslEnabled());
            output.setStarTlsEnabled(serverSettings.getServerEmailDetails().isStarTlsEnabled());
            output.setFrom(serverSettings.getServerEmailDetails().getFrom());
            if (serverSettings.getServerEmailDetails().getAuthEnabled()) {
                output.setUsername(serverSettings.getServerEmailDetails().getUsername());
				/* Password field not provided in response */
            }
            resource.setServerEmailResource(output);
        }
        return resource;
    }
}
