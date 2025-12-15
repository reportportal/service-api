/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.infrastructure.persistence.commons.querygen;

import static com.epam.reportportal.infrastructure.persistence.commons.Predicates.or;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.countOfValues;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.filterForArrayAggregation;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.filterForDates;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.filterForLogLevel;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.filterForLtree;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.filterForNumbers;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.filterForString;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.timeStamp;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterRules.zoneOffset;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.fail;
import static com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.INCORRECT_FILTER_PARAMETERS;
import static java.lang.Long.parseLong;
import static java.util.Date.from;
import static org.jooq.impl.DSL.any;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.function;
import static org.jooq.util.postgres.PostgresDSL.arrayLength;
import static org.jooq.util.postgres.PostgresDSL.arrayRemove;

import com.epam.reportportal.infrastructure.persistence.commons.Predicates;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.jooq.Field;
import org.jooq.Operator;
import org.jooq.impl.DSL;
import org.jooq.util.postgres.PostgresDSL;

/**
 * Types of supported filtering
 *
 * @author Andrei Varabyeu
 */
public enum Condition {

  /**
   * EQUALS condition
   */
  EQUALS("eq") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return field(criteriaHolder.getAggregateCriteria()).eq(this.castValue(criteriaHolder,
          filter.getValue(),
          INCORRECT_FILTER_PARAMETERS
      ));
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(isNegative, val -> Objects.equals(val, false)).verify(errorType,
          "Filter is incorrect. '!' can't be used with 'eq' - use 'ne' instead"
      );
      expect(criteriaHolder, Predicates.not(filterForArrayAggregation())).verify(errorType,
          "Equals condition not applicable for fields that have to be aggregated before filtering. Use 'HAS' or 'ANY'"
      );
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return criteriaHolder.castValue(value, errorType);
    }
  },

  /**
   * Not equals condition
   */
  NOT_EQUALS("ne") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return field(criteriaHolder.getAggregateCriteria()).ne(this.castValue(criteriaHolder,
          filter.getValue(),
          INCORRECT_FILTER_PARAMETERS
      ));
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, Predicates.not(filterForArrayAggregation())).verify(
          errorType,
          "Not equals condition not applicable for fields that have to be aggregated before filtering. Use 'HAS' or 'ANY'"
      );
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return criteriaHolder.castValue(value, errorType);
    }
  },

  /**
   * Contains operation. Case insensitive
   */
  CONTAINS("cnt") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      /* Validate only strings */

      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return field(criteriaHolder.getAggregateCriteria()).likeIgnoreCase(
              DSL.inline("%" + DSL.escape(filter.getValue(), '\\') + "%"))
          .and(field(criteriaHolder.getAggregateCriteria()).isNotNull());
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, filterForString()).verify(errorType, formattedSupplier(
          "Contains condition applicable only for strings. Type of field is '{}'",
          criteriaHolder.getDataType().getSimpleName()
      ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String values, ErrorType errorType) {
      // values cast is not required here
      return values;
    }
  },

  UNDER("under") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      validate(criteriaHolder, filter.getValue(), false, INCORRECT_FILTER_PARAMETERS);
      return DSL.condition(
          DSL.inline(filter.getValue()) + " @> " + criteriaHolder.getAggregateCriteria());
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, filterForLtree()).verify(errorType, formattedSupplier(
          "'Under' condition is applicable only for 'path' filter condition. Type of field is '{}'",
          criteriaHolder.getFilterCriteria()
      ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return value;
    }
  },

  LEVEL("level") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return function("nlevel", Long.class, field(criteriaHolder.getAggregateCriteria())).eq(
          (Long) this.castValue(criteriaHolder,
              filter.getValue(),
              INCORRECT_FILTER_PARAMETERS
          ));
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, or(filterForNumbers(), filterForLtree())).verify(errorType,
          formattedSupplier(
              "'Level' condition is applicable only for positive Numbers and 'path' filter condition. Type of field is '{}'",
              criteriaHolder.getDataType().getSimpleName()
          ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return criteriaHolder.castValue(value, errorType);
    }
  },

  /**
   * Exists condition
   */
  EXISTS("ex") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      if (criteriaHolder.getQueryCriteria().equals(criteriaHolder.getAggregateCriteria())) {
        return field(criteriaHolder.getAggregateCriteria()).isNotNull();
      } else {
        boolean exists = BooleanUtils.toBoolean(filter.getValue());
        Field<Integer> aggregatedCount = DSL.coalesce(
            arrayLength(arrayRemove(DSL.arrayAgg(field(criteriaHolder.getQueryCriteria())),
                (String) null
            )), 0);
        return exists ? aggregatedCount.gt(0) : aggregatedCount.eq(0);
      }
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      // object type validations is not required here
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String values, ErrorType errorType) {
      // values cast is not required here
      return values;
    }
  },

  /**
   * IN condition. Accepts filter value as comma-separated list
   */
  IN("in") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return field(criteriaHolder.getAggregateCriteria()).in(castValue(criteriaHolder,
          filter.getValue(),
          INCORRECT_FILTER_PARAMETERS
      ));
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, Predicates.not(filterForArrayAggregation())).verify(errorType,
          "In condition not applicable for fields that have to be aggregated before filtering. Use 'HAS' or 'ANY'"
      );
    }

    @Override
    public Object[] castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return castArray(criteriaHolder, value, errorType);
    }
  },

  /**
   * IN condition. Accepts filter value as comma-separated list
   */
  EQUALS_ANY("ea") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return field(criteriaHolder.getAggregateCriteria()).eq(any(DSL.array(castValue(criteriaHolder,
          filter.getValue(),
          INCORRECT_FILTER_PARAMETERS
      ))));
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, Predicates.not(filterForArrayAggregation())).verify(
          errorType,
          "Equals any condition not applicable for fields that have to be aggregated before filtering. Use 'HAS' or 'ANY'"
      );
    }

    @Override
    public Object[] castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return castArray(criteriaHolder, value, errorType);
    }
  },

  /**
   * HAS condition. Accepts filter value as comma-separated list. Returns 'TRUE' of all provided values exist in
   * collection<br>
   * <b>Applicable only for collections</b>
   */
  HAS("has") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      /* Validate only collections */
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      if (String.class.equals(criteriaHolder.getDataType())) {
        return DSL.condition(Operator.AND,
            field(criteriaHolder.getAggregateCriteria())
                .contains(DSL.arrayAgg(DSL.val(this.castValue(criteriaHolder,
                    filter.getValue(),
                    INCORRECT_FILTER_PARAMETERS
                )).cast(String[].class)))
        );
      }
      if (String[].class.equals(criteriaHolder.getDataType())) {
        return DSL.condition(Operator.AND,
            field(criteriaHolder.getAggregateCriteria())
                .contains(DSL.cast(
                    this.castValue(criteriaHolder, filter.getValue(), INCORRECT_FILTER_PARAMETERS),
                    String[].class
                ))
        );
      }
      return DSL.condition(Operator.AND,
          field(criteriaHolder.getAggregateCriteria())
              .contains(DSL.array((Object[]) this.castValue(criteriaHolder, filter.getValue(),
                  INCORRECT_FILTER_PARAMETERS)))
      );

    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, filterForArrayAggregation()).verify(errorType,
          "'HAS' condition applicable only for fields that have to be aggregated."
      );
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return castArray(criteriaHolder, value, errorType);
    }
  },

  /**
   * Overlap condition between two arrays
   */
  ANY("any") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      if (String.class.equals(criteriaHolder.getDataType())) {
        return DSL.condition(Operator.AND,
            PostgresDSL.arrayOverlap(
                field(criteriaHolder.getAggregateCriteria(), String[][].class),
                DSL.array(DSL.val(
                        this.castValue(criteriaHolder, filter.getValue(), INCORRECT_FILTER_PARAMETERS))
                    .cast(String[].class))
            )
        );

      }
      if (String[].class.equals(criteriaHolder.getDataType())) {
        return DSL.condition(Operator.AND,
            PostgresDSL.arrayOverlap(
                field(criteriaHolder.getAggregateCriteria(), String[].class),
                DSL.cast(
                    this.castValue(criteriaHolder, filter.getValue(), INCORRECT_FILTER_PARAMETERS),
                    String[].class)
            )
        );
      }
      return DSL.condition(Operator.AND,
          PostgresDSL.arrayOverlap(field(criteriaHolder.getAggregateCriteria(), Object[].class),
              DSL.array((Object[]) this.castValue(criteriaHolder, filter.getValue(),
                  INCORRECT_FILTER_PARAMETERS))
          ));
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, filterForArrayAggregation()).verify(errorType,
          "'ANY' condition applicable only for fields that have to be aggregated."
      );
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String values, ErrorType errorType) {
      return castArray(criteriaHolder, values, errorType);
    }
  },

  /**
   * Greater than condition
   */
  GREATER_THAN("gt") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      /* Validate only numbers & dates */
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return field(criteriaHolder.getAggregateCriteria()).greaterThan(this.castValue(criteriaHolder,
          filter.getValue(),
          INCORRECT_FILTER_PARAMETERS
      ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, or(filterForNumbers(), filterForDates(), filterForLogLevel())).verify(
          errorType, formattedSupplier(
              "'Greater than' condition applicable only for positive Numbers or Dates. Type of field is '{}'",
              criteriaHolder.getDataType().getSimpleName()
          ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return criteriaHolder.castValue(value, errorType);
    }
  },

  /**
   * Greater than or Equals condition
   */
  GREATER_THAN_OR_EQUALS("gte") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      /* Validate only numbers & dates */
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return field(criteriaHolder.getAggregateCriteria()).greaterOrEqual(
          this.castValue(criteriaHolder,
              filter.getValue(),
              INCORRECT_FILTER_PARAMETERS
          ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, or(filterForNumbers(), filterForDates(), filterForLogLevel())).verify(
          errorType, formattedSupplier(
              "'Greater than or equals' condition applicable only for positive Numbers or Dates. Type of field is '{}'",
              criteriaHolder.getDataType().getSimpleName()
          ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return criteriaHolder.castValue(value, errorType);
    }
  },

  /**
   * Lower than condition
   */
  LOWER_THAN("lt") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      /* Validate only numbers & dates */
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);

      return field(criteriaHolder.getAggregateCriteria()).lessThan(this.castValue(criteriaHolder,
          filter.getValue(),
          INCORRECT_FILTER_PARAMETERS
      ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, or(filterForNumbers(), filterForDates(), filterForLogLevel())).verify(
          errorType, formattedSupplier(
              "'Lower than' condition applicable only for positive Numbers or Dates. Type of field is '{}'",
              criteriaHolder.getDataType().getSimpleName()
          ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return criteriaHolder.castValue(value, errorType);
    }
  },

  /**
   * Lower than or Equals condition
   */
  LOWER_THAN_OR_EQUALS("lte") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      /* Validate only numbers & dates */
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);

      return field(criteriaHolder.getAggregateCriteria()).lessOrEqual(this.castValue(criteriaHolder,
          filter.getValue(),
          INCORRECT_FILTER_PARAMETERS
      ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, or(filterForNumbers(), filterForDates(), filterForLogLevel())).verify(
          errorType, formattedSupplier(
              "'Lower than or equals' condition applicable only for positive Numbers or Dates. Type of field is '{}'",
              criteriaHolder.getDataType().getSimpleName()
          ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      return criteriaHolder.castValue(value, errorType);
    }
  },

  /**
   * Between condition. Include boundaries
   */
  BETWEEN("btw") {
    @Override
    @SuppressWarnings("unchecked")
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, or(filterForNumbers(), filterForDates(), filterForLogLevel())).verify(
          errorType,
          formattedSupplier(
              "'Between' condition applicable only for positive Numbers, Dates or specific TimeStamp values. "
                  + "Type of field is '{}'", criteriaHolder.getDataType().getSimpleName())
      );
      if (value.contains(VALUES_SEPARATOR)) {
        expect(value.split(VALUES_SEPARATOR), countOfValues(2)).verify(errorType,
            formattedSupplier(
                "Incorrect between filter format. Expected='value1,value2'. Provided filter is '{}'",
                value)
        );
      } else if (value.contains(TIMESTAMP_SEPARATOR)) {
        final String[] values = value.split(TIMESTAMP_SEPARATOR);

        expect(values, countOfValues(BETWEEN_FILTER_VALUES_COUNT)).verify(errorType,
            formattedSupplier(
                "Incorrect between filter format. Expected='TIMESTAMP_CONSTANT;TimeZoneOffset'. Provided filter is '{}'",
                value
            ));
        expect(values[ZONE_OFFSET_INDEX], zoneOffset()).verify(errorType,
            formattedSupplier(
                "Incorrect zoneOffset. Expected='+h, +hh, +hh:mm'. Provided value is '{}'",
                values[ZONE_OFFSET_INDEX]
            )
        );
        expect(values[ZERO_TIMESTAMP_INDEX], timeStamp()).verify(errorType,
            formattedSupplier("Incorrect timestamp. Expected number. Provided value is '{}'",
                values[ZERO_TIMESTAMP_INDEX])
        );
        expect(values[FIRST_TIMESTAMP_INDEX], timeStamp()).verify(errorType,
            formattedSupplier("Incorrect timestamp. Expected number. Provided value is '{}'",
                values[FIRST_TIMESTAMP_INDEX])
        );
      } else {
        fail().withError(errorType, formattedSupplier(
            "Incorrect between filter format. Filter value should be separated by ',' or ';'. Provided filter is '{}'",
            value
        ));
      }
    }

    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      Object[] castedValues;
      if (filter.getValue().contains(",")) {
        castedValues = (Object[]) this.castValue(criteriaHolder, filter.getValue(),
            INCORRECT_FILTER_PARAMETERS);
      } else {
        String[] values = filter.getValue().split(";");
        ZoneOffset offset = ZoneOffset.of(values[2]);
        ZonedDateTime localDateTime = ZonedDateTime.now(offset).toLocalDate().atStartOfDay(offset);
        long start = from(localDateTime.plusMinutes(parseLong(values[0])).toInstant()).getTime();
        long end = from(localDateTime.plusMinutes(parseLong(values[1])).toInstant()).getTime();
        String newValue = start + "," + end;
        castedValues = (Object[]) this.castValue(criteriaHolder, newValue,
            INCORRECT_FILTER_PARAMETERS);
      }
      return DSL.condition(Operator.AND,
          field(criteriaHolder.getAggregateCriteria()).greaterOrEqual(castedValues[0]),
          field(criteriaHolder.getAggregateCriteria()).lessOrEqual(castedValues[1])
      );
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      /* For linguistic dynamic date range literals */
      if (value.contains(";")) {
        return value;
      } else {
        return castArray(criteriaHolder, value, errorType);
      }
    }
  },

  /**
   * Full text search condition using PostgreSQL tsvector
   */
  FULL_TEXT_SEARCH("fts") {
    @Override
    public org.jooq.Condition toCondition(FilterCondition filter, CriteriaHolder criteriaHolder) {
      this.validate(criteriaHolder, filter.getValue(), filter.isNegative(),
          INCORRECT_FILTER_PARAMETERS);
      return DSL.condition(
          "{0} @@ plainto_tsquery('simple', {1})",
          DSL.field(criteriaHolder.getAggregateCriteria()),
          DSL.val(filter.getValue())
      );
    }

    @Override
    public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
        ErrorType errorType) {
      expect(criteriaHolder, filterForString()).verify(errorType, formattedSupplier(
          "Full text search condition applicable only for search_vector fields. Type of field is '{}'",
          criteriaHolder.getDataType().getSimpleName()
      ));
    }

    @Override
    public Object castValue(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
      // value cast is not required for full text search
      return value;
    }
  };

  /*
   * Workaround. Added to be able to use as constant in annotations
   */
  public static final String EQ = "eq.";
  public static final String CNT = "cnt.";
  public static final String HAS_FILTER = "has.";
  public static final String UNDR = "under.";
  public static final String FTS = "fts.";

  public static final String VALUES_SEPARATOR = ",";
  public static final String TIMESTAMP_SEPARATOR = ";";
  public static final String NEGATIVE_MARKER = "!";
  public static final Integer BETWEEN_FILTER_VALUES_COUNT = 3;
  public static final Integer ZERO_TIMESTAMP_INDEX = 0;
  public static final Integer FIRST_TIMESTAMP_INDEX = 1;
  public static final Integer ZONE_OFFSET_INDEX = 2;

  private String marker;

  Condition(final String marker) {
    this.marker = marker;
  }

  /**
   * Finds condition by marker. If there is no condition with specified marker returns NULL
   *
   * @param marker Marker to be checked
   * @return Condition if found or NULL
   */
  public static Optional<Condition> findByMarker(String marker) {
    // Negative condition excluder
    if (isNegative(marker)) {
      marker = marker.substring(1);
    }
    String finalMarker = marker;
    return Arrays.stream(values()).filter(it -> it.getMarker().equals(finalMarker)).findAny();
  }

  /**
   * Check whether condition is negative
   *
   * @param marker Marker to check
   * @return TRUE of negative
   */
  public static boolean isNegative(String marker) {
    return marker.startsWith(NEGATIVE_MARKER);
  }

  /**
   * Makes filter marker negative
   *
   * @param negative Whether condition is negative
   * @param marker   Marker to check
   * @return TRUE of negative
   */
  public static String makeNegative(boolean negative, String marker) {
    String result;
    if (negative) {
      result = marker.startsWith(NEGATIVE_MARKER) ? marker : NEGATIVE_MARKER.concat(marker);
    } else {
      result = marker.startsWith(NEGATIVE_MARKER) ? marker.substring(1, marker.length()) : marker;
    }
    return result;

  }

  abstract public org.jooq.Condition toCondition(FilterCondition filter,
      CriteriaHolder criteriaHolder);

  /**
   * Validate condition value. This method should be overridden in all conditions which contains validations
   *
   * @param criteriaHolder Criteria description
   * @param value          Value to be casted
   * @param isNegative     Whether filter is negative
   */
  abstract public void validate(CriteriaHolder criteriaHolder, String value, boolean isNegative,
      ErrorType errorType);

  /**
   * Cast filter values according condition.
   *
   * @param criteriaHolder Criteria description
   * @param values         Value to be casted
   * @return Casted value
   */
  abstract public Object castValue(CriteriaHolder criteriaHolder, String values,
      ErrorType errorType);

  public String getMarker() {
    return marker;
  }

  /**
   * Cast values for filters which have many values(filters with conditions:btw, in, etc.)
   *
   * @param criteriaHolder Criteria description
   * @param value          Value to be casted
   * @return Casted value
   */
  public Object[] castArray(CriteriaHolder criteriaHolder, String value, ErrorType errorType) {
    String[] values = value.split(VALUES_SEPARATOR);
    Object[] castedValues = new Object[values.length];
    if (!String.class.equals(criteriaHolder.getDataType()) && !String[].class.equals(
        criteriaHolder.getDataType())) {
      for (int index = 0; index < values.length; index++) {
        castedValues[index] = criteriaHolder.castValue(values[index].trim(), errorType);
      }
    } else {
      castedValues = values;
    }
    return castedValues;
  }
}
