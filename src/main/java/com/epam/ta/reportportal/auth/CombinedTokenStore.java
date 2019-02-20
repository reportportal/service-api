/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.OAuth2AccessTokenRepository;
import com.epam.ta.reportportal.entity.user.StoredAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component(value = "combinedTokenStore")
@Transactional
public class CombinedTokenStore extends JwtTokenStore {

	@Autowired
	private OAuth2AccessTokenRepository oAuth2AccessTokenRepository;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	public CombinedTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
		super(jwtTokenEnhancer);
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		try {
			return super.readAuthentication(token);
		} catch (Exception e) {
			return this.readAuthentication(token.getValue());
		}
	}

	@Override
	public OAuth2Authentication readAuthentication(String tokenId) {
		try {
			return super.readAuthentication(tokenId);
		} catch (Exception e) {
			StoredAccessToken accessToken = oAuth2AccessTokenRepository.findByTokenId(tokenId);
			ReportPortalUser userDetails = (ReportPortalUser) userDetailsService.loadUserByUsername(accessToken.getUserName());
			OAuth2Authentication authentication = SerializationUtils.deserialize(accessToken.getAuthentication());
			ReportPortalUser reportPortalUser = (ReportPortalUser) authentication.getPrincipal();
			reportPortalUser.setProjectDetails(userDetails.getProjectDetails());
			reportPortalUser.setUserRole(userDetails.getUserRole());
			return authentication;
		}
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		try {
			return super.readAccessToken(tokenValue);
		} catch (Exception e) {
			StoredAccessToken token = oAuth2AccessTokenRepository.findByTokenId(tokenValue);
			if (token == null) {
				return null; //let spring security handle the invalid token
			}
			return SerializationUtils.deserialize(token.getToken());
		}
	}
}
