package com.epam.ta.reportportal.store.commons.querygen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import static com.epam.ta.reportportal.store.commons.querygen.QueryBuilder.filterConverter;

/**
 * Filter for building queries to database. Contains CriteriaHolder which is mapping between request
 * search criterias and DB search criterias and value to be filtered
 *
 * @author Andrei Varabyeu
 */
public class Filter implements Serializable, Queryable {

	private static final long serialVersionUID = 1L;

	private FilterTarget target;

	private Set<FilterCondition> filterConditions;

	public Filter(Class<?> target, Condition condition, boolean negative, String value, String searchCriteria) {
		this(FilterTarget.findByClass(target), Sets.newHashSet(new FilterCondition(condition, negative, value, searchCriteria)));
	}

	public Filter(Class<?> target, Set<FilterCondition> filterConditions) {
		this(FilterTarget.findByClass(target), filterConditions);
	}

	protected Filter(FilterTarget target, Set<FilterCondition> filterConditions) {
		Assert.notNull(target, "Filter target shouldn't be null");
		Assert.notNull(filterConditions, "Conditions value shouldn't be null");
		this.target = target;
		this.filterConditions = filterConditions;
	}

	/**
	 * This constructor uses during serialization to database.
	 */
	@SuppressWarnings("unused")
	private Filter() {

	}

	protected final FilterTarget getTarget() {
		return target;
	}

	public Filter withCondition(FilterCondition filterCondition) {
		this.filterConditions.add(filterCondition);
		return this;
	}

	public Filter withConditions(Collection<FilterCondition> conditions) {
		this.filterConditions.addAll(conditions);
		return this;
	}

	public SelectQuery<? extends Record> toQuery() {
		/* Get map of defined @FilterCriteria fields */
		final Function<FilterCondition, org.jooq.Condition> transformer = filterConverter(this.target);
		QueryBuilder query = QueryBuilder.newBuilder(this.target);
		this.filterConditions.stream().map(transformer).forEach(query::addCondition);
		return query.build();
	}

	public Set<FilterCondition> getFilterConditions() {
		return filterConditions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filterConditions == null) ? 0 : filterConditions.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		Filter other = (Filter) obj;
		if (filterConditions == null) {
			if (other.filterConditions != null) {
				return false;
			}
		} else if (!filterConditions.equals(other.filterConditions)) {
			return false;
		}
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Filter{");
		sb.append("target=").append(target);
		sb.append(", filterConditions=").append(filterConditions);
		sb.append('}');
		return sb.toString();
	}

	public static FilterBuilder builder() {
		return new FilterBuilder();
	}

	/**
	 * Builder for {@link Filter}
	 */
	public static class FilterBuilder {
		private Class<?> target;
		private ImmutableSet.Builder<FilterCondition> conditions = ImmutableSet.builder();

		private FilterBuilder() {

		}

		public FilterBuilder withTarget(Class<?> target) {
			this.target = target;
			return this;
		}

		public FilterBuilder withCondition(FilterCondition condition) {
			this.conditions.add(condition);
			return this;
		}

		public Filter build() {
			Set<FilterCondition> filterConditions = this.conditions.build();
			Preconditions.checkArgument(null != target, "FilterTarget should not be null");
			Preconditions.checkArgument(!filterConditions.isEmpty(), "Filter should contain at least one condition");
			return new Filter(target, filterConditions);
		}
	}

}