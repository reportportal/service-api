package com.epam.ta.reportportal.store.commons.querygen;

import com.epam.ta.reportportal.store.database.entity.enums.LogLevel;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.function.Predicate;

/**
 * Set of predicates which may be applied to the query builder and filter
 * conditions<br>
 *
 * @see com.epam.ta.reportportal.database.search.Condition
 * @see com.epam.ta.reportportal.database.search.QueryBuilder
 *
 * @author Andrei Varabyeu
 *
 */
public class FilterRules {

	private FilterRules() {

	}

	/**
	 * Accepts only strings as data type
	 *
	 * @return Predicate
	 */
	public static Predicate<CriteriaHolder> filterForString() {
		return filter -> String.class.equals(filter.getDataType());
	}

	/**
	 * Accepts only strings as data type
	 *
	 * @return Predicate
	 */
	public static Predicate<CriteriaHolder> filterForBoolean() {
		return filter -> boolean.class.equals(filter.getDataType()) || Boolean.class.isAssignableFrom(filter.getDataType());
	}

	public static Predicate<CriteriaHolder> filterForLogLevel() {
		return filter -> LogLevel.class.isAssignableFrom(filter.getDataType());
	}

	/**
	 * Accepts numbers only numbers as data type
	 *
	 * @return Predicate
	 */
	public static Predicate<CriteriaHolder> filterForNumbers() {
		return filter -> Number.class.isAssignableFrom(filter.getDataType());
	}

	/**
	 * Accepts collections as data type
	 *
	 * @return Predicate
	 */
	public static Predicate<CriteriaHolder> filterForCollections() {
		return filter -> Collection.class.isAssignableFrom(filter.getDataType());
	}

	/**
	 * Accepts numbers only numbers as data type
	 *
	 * @return Predicate
	 */
	public static <T extends Number> Predicate<T> numberIsPositive() {
		return number -> number.longValue() >= 0;
	}

	/**
	 * Accepts numbers only
	 *
	 * @return Predicate
	 */
	public static Predicate<String> number() {
		return NumberUtils::isCreatable;
	}

	/**
	 * Accepts numbers only
	 *
	 * @return Predicate
	 */
	public static Predicate<String> dateInMillis() {
		return object -> {
			/*
			 * May be rewritten in future. I suppose date as long value
			 */
			return number().test(object);
		};
	}

	/**
	 * Accepts only dates as data type
	 *
	 * @return Predicate
	 */
	public static Predicate<CriteriaHolder> filterForDates() {
		return filter -> Date.class.isAssignableFrom(filter.getDataType());
	}

	/**
	 * Count of values provided to filter
	 *
	 * @return Predicate
	 */
	public static Predicate<String[]> countOfValues(final int count) {
		return values -> count == values.length;
	}

	public static Predicate<String> zoneOffset() {
		return value -> {
			if (value == null)
				return false;
			try {
				ZoneOffset.of(value);
			} catch (DateTimeException e) {
				return false;
			}
			return true;
		};
	}

	public static Predicate<String> timeStamp() {
		return value -> {
			if (value == null) {
				return false;
			}
			try {
				long offset = Long.parseLong(value);
				LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)).plusMinutes(offset);
			} catch (NumberFormatException | DateTimeException e) {
				return false;
			}
			return true;
		};
	}

}