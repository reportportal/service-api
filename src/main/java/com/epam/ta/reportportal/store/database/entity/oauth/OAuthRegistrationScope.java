package com.epam.ta.reportportal.store.database.entity.oauth;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Andrei Varabyeu
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "oauth_registration_scope", schema = "public")
public class OAuthRegistrationScope implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "registration_id", nullable = false)
	private OAuthRegistration registration;

	@Column(name = "scope", nullable = false, length = 256)
	private String scope;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public OAuthRegistration getRegistration() {
		return registration;
	}

	public void setRegistration(OAuthRegistration registration) {
		this.registration = registration;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		OAuthRegistrationScope that = (OAuthRegistrationScope) o;
		return Objects.equals(id, that.id) && Objects.equals(registration, that.registration) && Objects.equals(scope, that.scope);
	}

	@Override
	public int hashCode() {

		return Objects.hash(id, registration, scope);
	}
}
