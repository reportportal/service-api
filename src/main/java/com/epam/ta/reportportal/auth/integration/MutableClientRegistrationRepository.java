package com.epam.ta.reportportal.auth.integration;

import com.epam.ta.reportportal.store.database.dao.OAuthRegistrationRepository;
import com.epam.ta.reportportal.store.database.entity.oauth.OAuthRegistration;
import com.epam.ta.reportportal.store.database.entity.oauth.OAuthRegistrationScope;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class MutableClientRegistrationRepository implements ClientRegistrationRepository {
	private OAuthRegistrationRepository oAuthRegistrationRepository;

	public static final Collector<ClientRegistration, ?, Map<String, ClientRegistration>> KEY_MAPPER = Collectors.toMap(ClientRegistration::getRegistrationId,
			r -> r
	);

	public static final Function<OAuthRegistration, ClientRegistration> REGISTRATION_MAPPER = registration -> ClientRegistration.withRegistrationId(
			registration.getClientName())
			.authorizationGrantType(new AuthorizationGrantType(registration.getAuthGrantType()))
			.authorizationUri(registration.getAuthorizationUri())
			.tokenUri(registration.getTokenUri())
			.clientAuthenticationMethod(new ClientAuthenticationMethod(registration.getClientAuthMethod()))
			.clientId(registration.getClientId())
			.clientSecret(registration.getClientSecret())
			.clientName(registration.getClientName())
			.scope(registration.getScopes().stream().map(OAuthRegistrationScope::getScope).toArray(String[]::new))
			.redirectUriTemplate("{baseUrl}/oauth2/authorization")
			.build();

	public static final Function<ClientRegistration, OAuthRegistration> REGISTRATION_REVERSE_MAPPER = fullRegistration -> {
		OAuthRegistration registration = new OAuthRegistration();
		registration.setId(fullRegistration.getRegistrationId());

		registration.setAuthGrantType(fullRegistration.getAuthorizationGrantType().getValue());
		registration.setClientAuthMethod(fullRegistration.getClientAuthenticationMethod().getValue());
		registration.setClientId(fullRegistration.getClientId());
		registration.setClientSecret(fullRegistration.getClientSecret());

		registration.setClientName(fullRegistration.getClientName());

		Optional.ofNullable(fullRegistration.getProviderDetails()).ifPresent(details -> {
			registration.setAuthorizationUri(details.getAuthorizationUri());
			registration.setTokenUri(details.getTokenUri());
			registration.setUserInfoEndpointUri(details.getUserInfoEndpoint().getUri());
			registration.setUserInfoEndpointNameAttribute(details.getUserInfoEndpoint().getUserNameAttributeName());
			registration.setJwkSetUri(details.getJwkSetUri());
		});
		return registration;
	};

	public MutableClientRegistrationRepository(OAuthRegistrationRepository oAuthRegistrationRepository) {
		this.oAuthRegistrationRepository = oAuthRegistrationRepository;
	}

	@Override
	public ClientRegistration findByRegistrationId(String registrationId) {
		return this.oAuthRegistrationRepository.findById(registrationId).map(REGISTRATION_MAPPER).orElse(null);
	}

	public boolean exists(String id) {
		return this.oAuthRegistrationRepository.existsById(id);

	}

	public ClientRegistration save(ClientRegistration registration) {
		return REGISTRATION_MAPPER.apply(this.oAuthRegistrationRepository.save(REGISTRATION_REVERSE_MAPPER.apply(registration)));
	}

	public void delete(String id) {
		oAuthRegistrationRepository.deleteById(id);
	}

	public Collection<ClientRegistration> findAll() {
		return this.oAuthRegistrationRepository.findAll().stream().map(REGISTRATION_MAPPER).collect(Collectors.toList());
	}

}
