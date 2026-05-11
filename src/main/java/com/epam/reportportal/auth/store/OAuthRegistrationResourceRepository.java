package com.epam.reportportal.auth.store;

import com.epam.reportportal.auth.model.OAuthRegistrationResource;

public interface OAuthRegistrationResourceRepository {

  OAuthRegistrationResource findOAuthRegistrationResourceById(String registrationId);
}