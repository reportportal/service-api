package com.epam.ta.reportportal.auth.integration;

import com.epam.ta.reportportal.store.jooq.tables.pojos.JOauthRegistration;
import com.epam.ta.reportportal.store.jooq.tables.records.JOauthRegistrationRecord;
import org.jooq.DSLContext;
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

import static com.epam.ta.reportportal.store.jooq.Tables.OAUTH_REGISTRATION_SCOPE;
import static com.epam.ta.reportportal.store.jooq.tables.JOauthRegistration.OAUTH_REGISTRATION;

@Component
public class MutableClientRegistrationRepository implements ClientRegistrationRepository {
	public static final Collector<ClientRegistration, ?, Map<String, ClientRegistration>> KEY_MAPPER = Collectors.toMap(ClientRegistration::getRegistrationId,
			r -> r
	);

	public static final Function<Map.Entry<JOauthRegistration, List<JOauthRegistrationScope>>, ClientRegistration> REGISTRATION_MAPPER = fullRegistration -> {
		JOauthRegistration registration = fullRegistration.getKey();
		return ClientRegistration.withRegistrationId(registration.getClientName())
				.authorizationGrantType(new AuthorizationGrantType(registration.getAuthGrantType()))
				.authorizationUri(registration.getAuthorizationUri())
				.tokenUri(registration.getTokenUri())
				.clientAuthenticationMethod(new ClientAuthenticationMethod(registration.getClientAuthMethod()))
				.clientId(registration.getClientId())
				.clientSecret(registration.getClientSecret())
				.clientName(registration.getClientName())
				.scope(fullRegistration.getValue().stream().map(OauthRegistrationScope::getScope).toArray(String[]::new))
				.redirectUriTemplate("{baseUrl}/oauth2/authorization")
				.build();

	};

	public static final Function<ClientRegistration, JOauthRegistration> REGISTRATION_REVERSE_MAPPER = fullRegistration -> {
		JOauthRegistration registration = new JOauthRegistration();
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
			registration.setUserInfoEndpointNameAttr(details.getUserInfoEndpoint().getUserNameAttributeName());
			registration.setJwkSetUri(details.getJwkSetUri());
		});
		return registration;
	};

	private final DSLContext dslContext;

	public MutableClientRegistrationRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	public ClientRegistration findByRegistrationId(String registrationId) {
		Map<JOauthRegistration, List<OauthRegistrationScope>> registration = dslContext.select()
				.from(OAUTH_REGISTRATION)
				.join(OAUTH_REGISTRATION_SCOPE)
				.on(OAUTH_REGISTRATION.ID.eq(OAUTH_REGISTRATION_SCOPE.OAUTH_REGISTRATION_FK))
				.where(OAUTH_REGISTRATION.ID.eq(registrationId))
				.fetchGroups(
						//map records first into the ROLE table and then into the value POJO type
						r -> r.into(JOauthRegistration.class), r -> r.into(OauthRegistrationScope.class));
		return REGISTRATION_MAPPER.apply(registration.entrySet().iterator().next());
	}

	public boolean exists(String id) {
		return dslContext.fetchExists(OAUTH_REGISTRATION, OAUTH_REGISTRATION.ID.eq(id));

	}

	public ClientRegistration save(ClientRegistration registration) {

		JOauthRegistrationRecord oauthRegistrationRecord = dslContext.insertInto(OAUTH_REGISTRATION)
				.values(REGISTRATION_REVERSE_MAPPER.apply(registration))
				.returning(OAUTH_REGISTRATION.ID)
				.fetchOne();

		dslContext.insertInto(OAUTH_REGISTRATION_SCOPE)
				.values(registration.getScopes()
						.stream()
						.map(s -> new OauthRegistrationScope(null, oauthRegistrationRecord.getId(), s))
						.collect(Collectors.toList()));

		return registration;
	}

	public boolean delete(String id) {
		return dslContext.delete(OAUTH_REGISTRATION).where(OAUTH_REGISTRATION.ID.eq(id)).execute() > 0;
	}

	public Collection<ClientRegistration> findAll() {
		return dslContext.select()
				.from(OAUTH_REGISTRATION)
				.fetchGroups(
						//map records first into the ROLE table and then into the value POJO type
						r -> r.into(JOauthRegistration.class), r -> r.into(OauthRegistrationScope.class))
				.entrySet()
				.stream()
				.map(REGISTRATION_MAPPER)
				.collect(Collectors.toList());
	}

}
