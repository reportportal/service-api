package com.epam.ta.reportportal.store.commons.querygen;

import com.epam.ta.reportportal.store.database.entity.enums.PostgreSQLEnumType;
import com.google.common.base.Preconditions;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Filter condition class for filters specifics
 */
@Entity
@Table(name = "filter_condition", schema = "public")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public class FilterCondition implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	public FilterCondition() {
	}

	public FilterCondition(Condition condition, boolean negative, String value, String searchCriteria) {
		super();
		this.condition = condition;
		this.value = value;
		this.searchCriteria = searchCriteria;
		this.negative = negative;
	}

	/**
	 * Filter Condition
	 */
	@Enumerated(EnumType.STRING)
	@Type(type = "pqsql_enum")
	@Column(name = "condition")
	private Condition condition;

	/**
	 * Value to be filtered
	 */
	@Column(name = "value")
	private String value;

	/**
	 * API Model Search Criteria
	 */
	@Column(name = "field")
	private String searchCriteria;

	/**
	 * Whether this is 'NOT' filter
	 */
	@Column(name = "negative")
	private boolean negative;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Condition getCondition() {
		return condition;
	}

	public String getSearchCriteria() {
		return searchCriteria;
	}

	public String getValue() {
		return value;
	}

	public boolean isNegative() {
		return negative;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + (negative ? 1231 : 1237);
		result = prime * result + ((searchCriteria == null) ? 0 : searchCriteria.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FilterCondition other = (FilterCondition) obj;
		if (condition != other.condition) {
			return false;
		}
		if (negative != other.negative) {
			return false;
		}
		if (searchCriteria == null) {
			if (other.searchCriteria != null) {
				return false;
			}
		} else if (!searchCriteria.equals(other.searchCriteria)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FilterCondition {").append("condition = ")
				.append(condition)
				.append(", value = ")
				.append(value)
				.append(", searchCriteria = ")
				.append(searchCriteria)
				.append(", negative = ")
				.append(negative)
				.append("}");
		return sb.toString();
	}

	public static ConditionBuilder builder() {
		return new ConditionBuilder();
	}

	/**
	 * Builder for {@link FilterCondition}
	 */
	public static class ConditionBuilder {
		private Condition condition;
		private boolean negative;
		private String value;
		private String searchCriteria;

		private ConditionBuilder() {

		}

		public FilterCondition.ConditionBuilder withCondition(Condition condition) {
			this.condition = condition;
			return this;
		}

		public FilterCondition.ConditionBuilder withNegative(boolean negative) {
			this.negative = negative;
			return this;
		}

		public FilterCondition.ConditionBuilder withValue(String value) {
			this.value = value;
			return this;
		}

		public FilterCondition.ConditionBuilder withSearchCriteria(String searchCriteria) {
			this.searchCriteria = searchCriteria;
			return this;
		}

		public FilterCondition.ConditionBuilder eq(String searchCriteria, String value) {
			return withCondition(Condition.EQUALS).withSearchCriteria(searchCriteria).withValue(value);
		}

		public FilterCondition build() {
			Preconditions.checkArgument(null != condition, "Condition should not be null");
			Preconditions.checkArgument(!isNullOrEmpty(value), "Value should not be empty");
			Preconditions.checkArgument(!isNullOrEmpty(searchCriteria), "Search criteria should not be empty");
			return new FilterCondition(condition, negative, value, searchCriteria);
		}
	}
}