package com.epam.ta.reportportal.store.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedPropertyUtilsTest {

	@Test
	void testToMap() {

		DemoBean bean = new DemoBean();
		bean.setNamedPersistentProperty("property 1");
		bean.setPersistentProperty("property 2");
		bean.setDateProperty(Instant.parse("2018-04-26T13:54:56.454Z"));
		bean.setIntProperty(100);

		Map<String, String> map = PersistedPropertyUtils.toMap(bean);
		assertThat(map).containsEntry("persistentProperty", "property 2").
				containsEntry("another name", "property 1").
				containsEntry("intProperty", "100").
				containsEntry("dateProperty", "2018-04-26T13:54:56.454Z");

		DemoBean b2 = new DemoBean();
		PersistedPropertyUtils.fromMap(map, b2);

		assertThat(b2.getDateProperty()).isEqualTo(Instant.parse("2018-04-26T13:54:56.454Z"));
		assertThat(b2.getIntProperty()).isEqualTo(100);
		assertThat(b2.getPersistentProperty()).isEqualTo("property 2");
		assertThat(b2.getNamedPersistentProperty()).isEqualTo("property 1");
	}

	@Test
	void testWrongType() {
		assertThrows(IllegalArgumentException.class, () -> PersistedPropertyUtils.toMap(new IncorrectBean()));
	}

	public static class DemoBean {

		@PersistedProperty
		private String persistentProperty;

		@PersistedProperty("another name")
		private String namedPersistentProperty;

		@PersistedProperty
		private Integer intProperty;

		@PersistedProperty
		private Instant dateProperty;

		public String getPersistentProperty() {
			return persistentProperty;
		}

		public void setPersistentProperty(String persistentProperty) {
			this.persistentProperty = persistentProperty;
		}

		public String getNamedPersistentProperty() {
			return namedPersistentProperty;
		}

		public void setNamedPersistentProperty(String namedPersistentProperty) {
			this.namedPersistentProperty = namedPersistentProperty;
		}

		public Integer getIntProperty() {
			return intProperty;
		}

		public void setIntProperty(Integer intProperty) {
			this.intProperty = intProperty;
		}

		public Instant getDateProperty() {
			return dateProperty;
		}

		public void setDateProperty(Instant dateProperty) {
			this.dateProperty = dateProperty;
		}

		@Override
		public String toString() {
			return "DemoBean{" + "persistentProperty='" + persistentProperty + '\'' + ", namedPersistentProperty='"
					+ namedPersistentProperty + '\'' + ", intProperty=" + intProperty + ", dateProperty=" + dateProperty + '}';
		}
	}

	public static class IncorrectBean {

		@PersistedProperty
		private Object unknownTypeProperty;

		public Object getUnknownTypeProperty() {
			return unknownTypeProperty;
		}

		public void setUnknownTypeProperty(Object unknownTypeProperty) {
			this.unknownTypeProperty = unknownTypeProperty;
		}
	}
}