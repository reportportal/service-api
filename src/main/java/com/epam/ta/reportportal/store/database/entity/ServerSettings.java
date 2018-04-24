package com.epam.ta.reportportal.store.database.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "server_settings", schema = "public")
public class ServerSettings implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "key", unique = true, nullable = false)
	private String key;

	@Column(name = "value")
	private String value;

	public ServerSettings() {
	}

	public ServerSettings(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ServerSettings that = (ServerSettings) o;
		return Objects.equals(id, that.id) && Objects.equals(key, that.key) && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, key, value);
	}
}
