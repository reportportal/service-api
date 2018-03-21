package com.epam.ta.reportportal.store.database.entity.oauth;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @author Andrei Varabyeu
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "oauth_registration", schema = "public")
public class OAuthRegistration implements Serializable {

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 64)
	private String id;

	@Column(name = "client_id", length = 128)
	private String clientId;

	@Column(name = "client_secret", length = 256)
	private String clientSecret;

	@Column(name = "client_auth_method", length = 64)
	private String clientAuthMethod;

	@Column(name = "auth_grant_type", length = 64)
	private String authGrantType;

	@Column(name = "redirect_uri_template", length = 256)
	private String redirectUrlTemplate;

	@Column(name = "authorization_uri", length = 256)
	private String authorizationUri;

	@Column(name = "token_uri", length = 256)
	private String tokenUri;

	@Column(name = "user_info_endpoint_uri", length = 256)
	private String userInfoEndpointUri;

	@Column(name = "user_info_endpoint_name_attr", length = 256)
	private String userInfoEndpointNameAttribute;

	@Column(name = "jwt_set_uri", length = 256)
	private String jwkSetUri;

	@Column(name = "client_name", length = 128)
	private String clientName;

	@OneToMany(mappedBy = "registration")
	private Set<OAuthRegistrationScope> scopes;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getClientAuthMethod() {
		return clientAuthMethod;
	}

	public void setClientAuthMethod(String clientAuthMethod) {
		this.clientAuthMethod = clientAuthMethod;
	}

	public String getAuthGrantType() {
		return authGrantType;
	}

	public void setAuthGrantType(String authGrantType) {
		this.authGrantType = authGrantType;
	}

	public String getRedirectUrlTemplate() {
		return redirectUrlTemplate;
	}

	public void setRedirectUrlTemplate(String redirectUrlTemplate) {
		this.redirectUrlTemplate = redirectUrlTemplate;
	}

	public String getAuthorizationUri() {
		return authorizationUri;
	}

	public void setAuthorizationUri(String authorizationUri) {
		this.authorizationUri = authorizationUri;
	}

	public String getTokenUri() {
		return tokenUri;
	}

	public void setTokenUri(String tokenUri) {
		this.tokenUri = tokenUri;
	}

	public String getUserInfoEndpointUri() {
		return userInfoEndpointUri;
	}

	public void setUserInfoEndpointUri(String userInfoEndpointUri) {
		this.userInfoEndpointUri = userInfoEndpointUri;
	}

	public String getUserInfoEndpointNameAttribute() {
		return userInfoEndpointNameAttribute;
	}

	public void setUserInfoEndpointNameAttribute(String userInfoEndpointNameAttribute) {
		this.userInfoEndpointNameAttribute = userInfoEndpointNameAttribute;
	}

	public String getJwkSetUri() {
		return jwkSetUri;
	}

	public void setJwkSetUri(String jwkSetUri) {
		this.jwkSetUri = jwkSetUri;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public Set<OAuthRegistrationScope> getScopes() {
		return scopes;
	}

	public void setScopes(Set<OAuthRegistrationScope> scopes) {
		this.scopes = scopes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		OAuthRegistration that = (OAuthRegistration) o;
		return Objects.equals(id, that.id) && Objects.equals(clientId, that.clientId) && Objects.equals(clientSecret, that.clientSecret)
				&& Objects.equals(clientAuthMethod, that.clientAuthMethod) && Objects.equals(authGrantType, that.authGrantType)
				&& Objects.equals(redirectUrlTemplate, that.redirectUrlTemplate) && Objects.equals(authorizationUri, that.authorizationUri)
				&& Objects.equals(tokenUri, that.tokenUri) && Objects.equals(userInfoEndpointUri, that.userInfoEndpointUri)
				&& Objects.equals(
				userInfoEndpointNameAttribute,
				that.userInfoEndpointNameAttribute
		) && Objects.equals(jwkSetUri, that.jwkSetUri) && Objects.equals(clientName, that.clientName) && Objects.equals(
				scopes,
				that.scopes
		);
	}

	@Override
	public int hashCode() {

		return Objects.hash(id,
				clientId,
				clientSecret,
				clientAuthMethod,
				authGrantType,
				redirectUrlTemplate,
				authorizationUri,
				tokenUri,
				userInfoEndpointUri,
				userInfoEndpointNameAttribute,
				jwkSetUri,
				clientName,
				scopes
		);
	}
}
