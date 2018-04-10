package com.epam.ta.reportportal.store.commons.querygen;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.store.commons.Predicates;
import com.epam.ta.reportportal.ws.model.ErrorType;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.of;

/**
 * Holds criteria mappings for specified class. After initialization reads
 * specified package and stores this information in Map where key is request
 * search criteria
 *
 * @author Andrei Varabyeu
 */
public class CriteriaMap<T> {
	public static final String SEARCH_CRITERIA_SEPARATOR = "$";
	public static final String QUERY_CRITERIA_SEPARATOR = ".";

	/**
	 * Request search criteria to criteria holder mapping
	 */
	private Map<String, CriteriaHolder> classCriteria;

	public CriteriaMap(Class<T> clazz) {
		// TODO check class is Mongo document
		classCriteria = new HashMap<>();
		lookupClass(clazz, new ArrayList<>());
	}

	private void lookupClass(Class<?> clazz, List<Field> parents) {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(FilterCriteria.class)) {
				boolean dynamicNestedFields = isDynamicInnerFields(f);
				String searchCriteria;
				String queryCriteria;
				Class<?> fieldType = f.getType();

				//grab generic type argument for collection types
				if (Collection.class.isAssignableFrom(fieldType) && f.getGenericType() instanceof ParameterizedType) {
					fieldType = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
				}
				if (of(fieldType.getDeclaredFields()).anyMatch(df -> df.isAnnotationPresent(FilterCriteria.class))
						|| (fieldType.isAnnotationPresent(Entity.class))) {
					List<Field> currentParents = new ArrayList<>(parents);
					searchCriteria = parents.isEmpty() ? getSearchCriteria(f) : getSearchCriteria(f, currentParents);
					queryCriteria = parents.isEmpty() ? getQueryCriteria(f) : getQueryCriteria(f, currentParents);
					classCriteria.put(searchCriteria,
							new CriteriaHolder(searchCriteria, queryCriteria, getDataType(f), dynamicNestedFields)
					);
					currentParents.add(f);
					lookupClass(fieldType, currentParents);
				} else {
					searchCriteria = getSearchCriteria(f);
					queryCriteria = getQueryCriteria(f);
					if (parents.isEmpty()) {
						classCriteria.put(searchCriteria,
								new CriteriaHolder(searchCriteria, queryCriteria, getDataType(f), dynamicNestedFields)
						);
					} else {
						searchCriteria = getSearchCriteria(f, parents);
						queryCriteria = getQueryCriteria(f, parents);
						classCriteria.put(getSearchCriteria(f, parents),
								new CriteriaHolder(searchCriteria, queryCriteria, getDataType(f), dynamicNestedFields)
						);
					}
				}
			}
		}
	}

	public static String getQueryCriteria(Field f) {
		String queryCriteria;
		if (f.isAnnotationPresent(Column.class)) {
			queryCriteria = f.getAnnotation(Column.class).name();
		} else {
			queryCriteria = f.getName();
		}
		return queryCriteria;
	}

	/**
	 * Returns holder for specified request search criteria
	 *
	 * @param searchCriteria Front-end search criteria
	 * @return Search criteria details
	 */
	public CriteriaHolder getCriteriaHolder(String searchCriteria) {
		Optional<CriteriaHolder> criteria = getCriteriaHolderUnchecked(searchCriteria);
		BusinessRule.expect(criteria, Predicates.isPresent())
				.verify(ErrorType.INCORRECT_FILTER_PARAMETERS, "Criteria '" + searchCriteria + "' not defined");
		return criteria.get();
	}

	/**
	 * Returns holder for specified request search criteria. If field contains dynamic part, uses 'starts with' condition
	 * Rebuild criteria if its dynamic
	 *
	 * @param searchCriteria Front-end search criteria
	 * @return Search criteria details
	 */
	public Optional<CriteriaHolder> getCriteriaHolderUnchecked(String searchCriteria) {
		Optional<CriteriaHolder> criteriaHolder = ofNullable(classCriteria.get(searchCriteria));
		if (!criteriaHolder.isPresent()) {
			return classCriteria.entrySet()
					.stream()
					.filter(it -> it.getValue().isHasDynamicPart())
					.filter(it -> searchCriteria.startsWith(it.getKey()))
					.findAny()
					.map(Map.Entry::getValue)
					.map(it -> {
						String dynamicPart = searchCriteria.substring(it.getFilterCriteria().length(), searchCriteria.length());
						String queryCriteria = it.getQueryCriteria() + dynamicPart.replace(CriteriaMap.SEARCH_CRITERIA_SEPARATOR,
								CriteriaMap.QUERY_CRITERIA_SEPARATOR
						);
						String filterCriteria = it.getFilterCriteria() + dynamicPart;
						return new CriteriaHolder(filterCriteria, queryCriteria, it.getDataType(), false);

					});
		}
		return criteriaHolder;
	}

	/**
	 * @return allowed search criteria values
	 */
	public Set<String> getAllowedSearchCriterias() {
		return this.classCriteria.keySet();
	}

	private Class<?> getDataType(Field f) {
		if (isDynamicInnerFields(f)) {
			return (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[1];
		} else {
			return f.getType();
		}
	}

	private String getSearchCriteria(Field f) {
		return f.getAnnotation(FilterCriteria.class).value();
	}

	private boolean isDynamicInnerFields(Field f) {
		return Map.class.isAssignableFrom(f.getType());
	}

	private String getSearchCriteria(Field f, List<Field> parents) {
		StringBuilder criteriaBuilder = new StringBuilder();
		for (Field parent : parents) {
			criteriaBuilder.append(getSearchCriteria(parent));
			criteriaBuilder.append(SEARCH_CRITERIA_SEPARATOR);
		}
		if (criteriaBuilder.length() > 0) {
			/* Remove last separator */
			return criteriaBuilder.append(getSearchCriteria(f)).toString();
		} else {
			return "";
		}
	}

	private String getQueryCriteria(Field f, List<Field> parents) {
		StringBuilder criteriaBuilder = new StringBuilder();
		for (Field parent : parents) {
			criteriaBuilder.append(getQueryCriteria(parent));
			criteriaBuilder.append(QUERY_CRITERIA_SEPARATOR);
		}
		if (criteriaBuilder.length() > 0) {
			/* Remove last separator */
			return criteriaBuilder.append(getQueryCriteria(f)).toString();
		} else {
			return "";
		}
	}

	@Override
	public String toString() {
		return "CriteriaMap [classCriteria=" + classCriteria + "]";
	}
}