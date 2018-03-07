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

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * Shows list of supported authentication providers
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class AuthProvidersInfoContributor implements InfoContributor {

//    private final ServerSettingsRepository settingsRepository;
//    private final Map<String, OAuthProvider> providersMap;

//    @Autowired
//    public AuthProvidersInfoContributor(ServerSettingsRepository settingsRepository,
//            Map<String, OAuthProvider> providersMap) {
//        this.settingsRepository = settingsRepository;
//        this.providersMap = providersMap;
//    }

    @Override
    public void contribute(Info.Builder builder) {
//        final Map<String, OAuth2LoginDetails> oauth2Details =
		//                settingsRepository.findOne("default").getoAuth2LoginDetails();
		//
		//        final Map<String, AuthProviderInfo> providers = providersMap.values()
		//                .stream()
		//                .filter(p -> !p.isConfigDynamic() || (null != oauth2Details && oauth2Details.containsKey(p.getName())))
		//                .collect(Collectors
		//                        .toMap(OAuthProvider::getName,
		//                                p -> new AuthProviderInfo(p.getButton(), p.buildPath(getAuthBasePath()))));
		//
		//        builder.withDetail("auth_extensions", providers);
        //@formatter:on
    }

//    private String getAuthBasePath() {
//        return fromCurrentContextPath().path(OAuthSecurityConfig.SSO_LOGIN_PATH).build().getPath();
//    }

    public static class AuthProviderInfo {
        private String button;
        private String path;

        public AuthProviderInfo(String button, String path) {
            this.button = button;
            this.path = path;
        }

        public String getButton() {
            return button;
        }

        public void setButton(String button) {
            this.button = button;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
