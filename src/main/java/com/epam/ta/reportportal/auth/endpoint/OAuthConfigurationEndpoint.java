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

import com.epam.ta.reportportal.auth.integration.MutableClientRegistrationRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Endpoint for oauth configs
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Controller
@RequestMapping("/settings/{profileId}/oauth")
@Api(description = "OAuth Configuration Endpoint")
public class OAuthConfigurationEndpoint {

	private final MutableClientRegistrationRepository clientRegistrations;

	@Autowired
	public OAuthConfigurationEndpoint(MutableClientRegistrationRepository clientRegistrations) {
		this.clientRegistrations = clientRegistrations;
	}

	/**
	 * Updates oauth integration settings
	 *
	 * @param clientDetails OAuth configuration
	 * @return All defined OAuth integration settings
	 */
	@RequestMapping(value = "/", method = { POST, PUT })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Creates/Updates OAuth Integration Settings")
	public ClientRegistration updateOAuthSettings(@RequestBody @Validated ClientRegistration clientDetails) {
		return clientRegistrations.save(clientDetails);
	}

	/**
	 * Deletes oauth integration settings
	 *
	 * @param clientID settings ProfileID
	 * @return All defined OAuth integration settings
	 */
	@RequestMapping(value = "/{authId}", method = { DELETE })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Deletes OAuth Integration Settings", notes = "'default' profile is using till additional UI implementations")
	public OperationCompletionRS deleteOAuthSetting(@PathVariable("authId") String clientID) {

		if (!clientRegistrations.delete(clientID)) {
			throw new ReportPortalException(ErrorType.OAUTH_INTEGRATION_NOT_FOUND);
		}

		return new OperationCompletionRS("Auth settings '" + clientID + "' were successfully removed");
	}

	/**
	 * Returns oauth integration settings
	 *
	 * @return All defined OAuth integration settings
	 */
	@RequestMapping(value = "/", method = { GET })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Returns OAuth Server Settings", notes = "'default' profile is using till additional UI implementations")
	public Map<String, ClientRegistration> getOAuthSettings() {
		return clientRegistrations.findAll().stream().collect(MutableClientRegistrationRepository.KEY_MAPPER);
	}

	/**
	 * Returns oauth integration settings
	 *
	 * @param oauthProviderName ID of third-party OAuth provider
	 * @return All defined OAuth integration settings
	 */
	@RequestMapping(value = "/{authId}", method = { GET })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Returns OAuth Server Settings", notes = "'default' profile is using till additional UI implementations")
	public ClientRegistration getOAuthSettings(@PathVariable("authId") String oauthProviderName) {
		return clientRegistrations.findByRegistrationId(oauthProviderName);
	}
}
