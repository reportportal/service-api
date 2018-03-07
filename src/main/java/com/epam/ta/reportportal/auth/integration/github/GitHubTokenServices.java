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
package com.epam.ta.reportportal.auth.integration.github;



/**
 * Token services for GitHub account info with internal ReportPortal's database
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
//public class GitHubTokenServices implements ResourceServerTokenServices {
public class GitHubTokenServices {

//	private final GitHubUserReplicator replicator;
//	private final Supplier<OAuth2ProtectedResourceDetails> loginDetails;
//
//	public GitHubTokenServices(GitHubUserReplicator replicatingPrincipalExtractor, Supplier<OAuth2ProtectedResourceDetails> loginDetails) {
//		this.replicator = replicatingPrincipalExtractor;
//		this.loginDetails = loginDetails;
//	}
//
//	@Override
//	public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
//		GitHubClient gitHubClient = GitHubClient.withAccessToken(accessToken);
//		UserResource gitHubUser = gitHubClient.getUser();
//
//		//TODO fix organizations
//		//        List<String> allowedOrganizations = ofNullable(loginDetails.get().getRestrictions())
//		//                .flatMap(restrictions -> ofNullable(restrictions.get("organizations")))
//		//                .map(it -> Splitter.on(",").omitEmptyStrings().splitToList(it))
//		//                .orElse(emptyList());
//		//        if (!allowedOrganizations.isEmpty()) {
//		//            boolean assignedToOrganization = gitHubClient.getUserOrganizations(gitHubUser).stream().map(userOrg -> userOrg.login)
//		//                    .anyMatch(allowedOrganizations::contains);
//		//            if (!assignedToOrganization) {
//		//                throw new InsufficientOrganizationException("User '" + gitHubUser.login + "' does not belong to allowed GitHUB organization");
//		//            }
//		//        }
//
//		//		Users user = replicator.replicateUser(gitHubUser, gitHubClient);
//		//
//		//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getId(),
//		//				"N/A",
//		//				AuthUtils.AS_AUTHORITIES.apply(UserRole.findByName(user.getRole().getName()).get())
//		//		);
//		//
//		//		Map<String, Serializable> extensionProperties = Collections.singletonMap("upstream_token", accessToken);
//		//		OAuth2Request request = new OAuth2Request(
//		//				null,
//		//				loginDetails.get().getClientId(),
//		//				null,
//		//				true,
//		//				null,
//		//				null,
//		//				null,
//		//				null,
//		//				extensionProperties
//		//		);
//		//		return new OAuth2Authentication(request, token);
//		return null;
//	}
//
//	@Override
//	public OAuth2AccessToken readAccessToken(String accessToken) {
//		throw new UnsupportedOperationException("Not supported: read access token");
//	}
//
//	public static class InsufficientOrganizationException extends AuthenticationException {
//
//		public InsufficientOrganizationException(String msg) {
//			super(msg);
//		}
//	}

}
