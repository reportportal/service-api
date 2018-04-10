package com.epam.ta.reportportal.store.commons.querygen;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static org.jooq.impl.DSL.field;

/**
 * MongoDB query builder. Constructs MongoDB
 * {@link org.jooq.Query} by provided filters <br>
 * <p>
 * TODO Some interface for QueryBuilder should be created to avoid problems with possible changing
 * of DB engine
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
public class QueryBuilder {

	/**
	 * SQL query representation
	 */
	private SelectQuery<? extends Record> query;

	private QueryBuilder(FilterTarget target) {
		query = target.getQuery();
	}

	private QueryBuilder(SelectQuery<? extends Record> query) {
		this.query = query;
	}

	public static QueryBuilder newBuilder(FilterTarget target) {
		return new QueryBuilder(target);
	}

	public static QueryBuilder newBuilder(Queryable queryable) {
		return new QueryBuilder(queryable.toQuery());
	}

	/**
	 * Adds condition to the query
	 *
	 * @param condition Condition
	 * @return QueryBuilder
	 */
	public QueryBuilder addCondition(Condition condition) {
		query.addConditions(condition);
		return this;
	}

	/**
	 * Adds {@link org.springframework.data.domain.Pageable} conditions
	 *
	 * @param p Pageable
	 * @return QueryBuilder
	 */
	public QueryBuilder with(Pageable p) {
		query.addLimit(p.getPageSize());
		query.addOffset(Long.valueOf(p.getOffset()).intValue());
		return this;
	}

	/**
	 * Add limit
	 *
	 * @param limit Limit
	 * @return QueryBuilder
	 */
	public QueryBuilder with(int limit) {
		query.addLimit(limit);
		return this;
	}

	/**
	 * Add sorting {@link Sort}
	 *
	 * @param sort Sort condition
	 * @return QueryBuilder
	 */
	public QueryBuilder with(Sort sort) {
		StreamSupport.stream(sort.spliterator(), false)
				.forEach(order -> query.addOrderBy(field(order.getProperty()).sort(order.getDirection().isDescending() ?
						SortOrder.DESC :
						SortOrder.ASC)));

		return this;
	}

	/**
	 * Builds query
	 *
	 * @return Query
	 */
	public SelectQuery<? extends Record> build() {
		return query;
	}

	public static Function<FilterCondition, Condition> filterConverter(FilterTarget target) {
		return filterCondition -> {
			Optional<CriteriaHolder> criteriaHolder = target.getCriteriaByFilter(filterCondition.getSearchCriteria());
			BusinessRule.expect(criteriaHolder, Preconditions.IS_PRESENT).verify(
					ErrorType.INCORRECT_FILTER_PARAMETERS,
					Suppliers.formattedSupplier("Filter parameter {} is not defined", filterCondition.getSearchCriteria())
			);

			Condition condition = filterCondition.getCondition().toCondition(filterCondition, criteriaHolder.get());
			/* Does FilterCondition contains negative=true? */
			if (filterCondition.isNegative()) {
				condition = condition.not();
			}
			return condition;
		};
	}
}